package com.bank.application.wiring

import com.bank.application.usecase.IngestTransaction
import com.bank.infra.persistence.gateway.BalanceGatewayImpl
import com.bank.infra.persistence.gateway.IdempotencyGatewayImpl
import com.bank.infra.persistence.gateway.TransactionGatewayImpl

object ApplicationWiring {

    private val transactionGateway = TransactionGatewayImpl()
    private val balanceGateway = BalanceGatewayImpl()
    private val idempotencyGateway = IdempotencyGatewayImpl()

    fun ingestTransaction(): IngestTransaction =
        IngestTransaction(
            transactionGateway = transactionGateway,
            balanceGateway = balanceGateway,
            idempotencyGateway = idempotencyGateway
        )

    fun balanceGateway() = balanceGateway
}
