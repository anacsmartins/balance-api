package com.bank.application.usecase

import com.bank.application.gateway.BalanceGateway
import com.bank.application.gateway.IdempotencyGateway
import com.bank.application.gateway.TransactionGateway
import com.bank.domain.model.Balance
import com.bank.domain.model.Money
import com.bank.domain.model.Transaction
import com.bank.infra.observability.LogContext
import com.bank.infra.observability.logger

class IngestTransaction(
    private val transactionGateway: TransactionGateway,
    private val balanceGateway: BalanceGateway,
    private val idempotencyGateway: IdempotencyGateway
) {

    private val log = logger<IngestTransaction>()

    fun execute(command: IngestTransactionCommand) {

        LogContext.withTransaction(
            transactionId = command.transactionId.toString(),
            accountId = command.accountId.toString()
        ) {

            log.info("Starting transaction ingestion")

            if (!idempotencyGateway.tryMarkAsProcessed(command.transactionId)) {
                log.warn("Transaction already processed, skipping ingestion")
                return@withTransaction
            }

            try {
                val transaction = Transaction(
                    id = command.transactionId,
                    accountId = command.accountId,
                    amount = Money.of(command.amount),
                    status = command.status,
                    occurredAt = command.occurredAt
                )
                transactionGateway.save(transaction)

                command.reportedAccountBalance.let { reported ->
                    balanceGateway.upsertSnapshot(
                        Balance(
                            accountId = command.accountId,
                            amount = Money.of(reported),
                            currency = command.reportedAccountCurrency,
                            updatedAt = command.occurredAt
                        )
                    )
                }

                log.info("Transaction successfully ingested")

            } catch (ex: IllegalArgumentException) {
                log.error("Invalid transaction payload", ex)
                throw ex

            } catch (ex: Exception) {
                log.error("Unexpected error during transaction ingestion", ex)
                throw ex
            }
        }
    }
}
