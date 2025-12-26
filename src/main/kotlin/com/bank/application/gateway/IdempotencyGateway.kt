package com.bank.application.gateway

import java.util.UUID

interface IdempotencyGateway {
    fun tryMarkAsProcessed(transactionId: UUID): Boolean
}


