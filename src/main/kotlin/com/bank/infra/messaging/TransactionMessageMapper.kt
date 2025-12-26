package com.bank.infra.messaging

import com.bank.application.usecase.IngestTransactionCommand
import java.time.Instant

object TransactionMessageMapper {

    fun from(message: TransactionEventMessage): IngestTransactionCommand {

        val balance = message.account.balance
            ?: throw IllegalArgumentException("Missing account.balance")

        return IngestTransactionCommand(
            transactionId = message.transaction.id,
            accountId = message.account.id,
            amount = message.transaction.amount,
            status = message.transaction.status,
            occurredAt = Instant.ofEpochMilli(
                message.transaction.timestamp / 1_000
            ),
            reportedAccountBalance = balance.amount
                ?: throw IllegalArgumentException("Missing balance.amount"),
            reportedAccountCurrency = balance.currency
                ?: throw IllegalArgumentException("Missing balance.currency")
        )
    }
}
