package com.bank.infra.messaging.payload

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class BalancePayload(
    val amount: BigDecimal?,
    val currency: String?
)