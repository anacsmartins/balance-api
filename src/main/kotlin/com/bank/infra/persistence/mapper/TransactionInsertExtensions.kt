package com.bank.infra.persistence.mapper

import com.bank.domain.model.Transaction
import com.bank.infra.persistence.table.TransactionTable
import org.jetbrains.exposed.sql.statements.InsertStatement

fun InsertStatement<Number>.mapFrom(transaction: Transaction) {
    this[TransactionTable.transactionId] = transaction.id
    this[TransactionTable.accountId] = transaction.accountId
    this[TransactionTable.amount] = transaction.amount.value
    this[TransactionTable.status] = transaction.status.name
    this[TransactionTable.occurredAt] = transaction.occurredAt
    this[TransactionTable.createdAt] = transaction.occurredAt
}
