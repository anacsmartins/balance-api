package com.bank.domain.model

import java.time.Instant
import java.util.UUID

data class Balance(
    val accountId: UUID,
    val amount: Money,
    val currency: String,
    val updatedAt: Instant
)
