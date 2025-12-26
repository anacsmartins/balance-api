package com.bank.infra.persistence.gateway

import com.bank.application.gateway.TransactionGateway
import com.bank.domain.model.Transaction
import com.bank.infra.persistence.mapper.TransactionMapper
import com.bank.infra.persistence.table.TransactionTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class TransactionGatewayImpl : TransactionGateway {

    override fun save(transaction: Transaction) {
        transaction {
            TransactionTable.insert {
                TransactionMapper.toInsert(it, transaction)
            }
        }
    }
}
