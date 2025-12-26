package com.bank.infra.persistence.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object IdempotencyTable : Table("processed_transactions") {

    val transactionId = uuid("transaction_id")
    val processedAt = timestamp("processed_at")

    override val primaryKey =
        PrimaryKey(transactionId, name = "pk_processed_transactions")
}
