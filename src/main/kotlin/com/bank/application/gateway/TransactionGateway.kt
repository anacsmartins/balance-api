package com.bank.application.gateway

import com.bank.domain.model.Transaction

interface TransactionGateway {
    fun save(transaction: Transaction)
}
