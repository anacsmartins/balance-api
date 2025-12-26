package com.bank.infra.persistence.mapper

import com.bank.domain.model.Transaction
import com.bank.infra.persistence.table.TransactionTable
import org.jetbrains.exposed.sql.statements.InsertStatement

object TransactionMapper {

    fun toInsert(
        statement: InsertStatement<Number>,
        transaction: Transaction
    ) {
        statement[TransactionTable.transactionId] = transaction.id
        statement[TransactionTable.accountId] = transaction.accountId
        statement[TransactionTable.amount] = transaction.amount.value
        statement[TransactionTable.status] = transaction.status.name
        statement[TransactionTable.occurredAt] = transaction.occurredAt
        statement[TransactionTable.createdAt] = transaction.occurredAt
    }
}
