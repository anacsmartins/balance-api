package com.bank.infra.messaging.payload

import com.bank.domain.enums.TransactionStatus
import com.bank.domain.enums.TransactionType
import java.math.BigDecimal
import java.util.UUID

data class TransactionPayload(
    val id: UUID,
    val type: TransactionType,
    val amount: BigDecimal,
    val currency: String,
    val status: TransactionStatus,
    val timestamp: Long
)