package com.bank.infra.messaging

import com.bank.infra.messaging.payload.AccountPayload
import com.bank.infra.messaging.payload.TransactionPayload
import java.math.BigDecimal
import java.util.UUID

data class TransactionEventMessage(
    val transaction: TransactionPayload,
    val account: AccountPayload
)
