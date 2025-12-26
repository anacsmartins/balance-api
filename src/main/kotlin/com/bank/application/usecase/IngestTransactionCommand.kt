package com.bank.application.usecase

import com.bank.domain.enums.TransactionStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class IngestTransactionCommand(
    val transactionId: UUID,
    val accountId: UUID,
    val amount: BigDecimal,
    val status: TransactionStatus,
    val occurredAt: Instant,
    val reportedAccountBalance: BigDecimal,
    val reportedAccountCurrency: String
) {

    init {
        require(amount >= BigDecimal.ZERO) {
            "Transaction amount must be >= 0"
        }

        require(reportedAccountBalance >= BigDecimal.ZERO) {
            "Reported account balance must be >= 0"
        }

        require(reportedAccountCurrency.isNotBlank()) {
            "Reported account currency must not be blank"
        }

        require(reportedAccountCurrency.length == 3) {
            "Reported account currency must be ISO-4217 (3 letters)"
        }
    }
}
