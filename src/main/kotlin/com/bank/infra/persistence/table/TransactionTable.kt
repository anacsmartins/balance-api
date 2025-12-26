package com.bank.infra.persistence.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object TransactionTable : Table("transactions") {

    val transactionId = uuid("transaction_id")
    val accountId = uuid("account_id")
    val amount = decimal("amount", 19, 2)
    val status = varchar("status", 20)
    val occurredAt = timestamp("occurred_at")
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(transactionId)
}
