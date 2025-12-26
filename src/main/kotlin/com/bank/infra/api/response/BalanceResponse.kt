package com.bank.infra.api.response

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class BalanceResponse(
    val accountId: UUID,
    val amount: BigDecimal,
    val currency: String,
    val updatedAt: Instant
)