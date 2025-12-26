package com.bank.infra.messaging.payload

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.UUID

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AccountPayload(
    val id: UUID,
    val owner: UUID,
    val createdAt: Long,
    val status: String,
    val balance: BalancePayload?
)
