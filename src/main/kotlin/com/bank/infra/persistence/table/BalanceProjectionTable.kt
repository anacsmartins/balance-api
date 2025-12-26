package com.bank.infra.persistence.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object BalanceProjectionTable : Table("balance_projection") {

    val accountId = uuid("account_id").uniqueIndex()
    val balance = decimal("balance", precision = 19, scale = 2)
    val updatedAt = timestamp("updated_at")
    val currency = varchar("currency", 3)

    override val primaryKey = PrimaryKey(accountId, name = "pk_balance_projection")
}
