package com.bank.domain.model

import com.bank.domain.enums.TransactionStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class Transaction(
    val id: UUID,
    val accountId: UUID,
    val amount: Money,
    val status: TransactionStatus,
    val occurredAt: Instant
)

