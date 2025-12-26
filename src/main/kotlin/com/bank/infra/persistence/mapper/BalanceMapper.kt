package com.bank.infra.persistence.mapper

import com.bank.domain.model.Balance
import com.bank.domain.model.Money
import com.bank.infra.persistence.table.BalanceProjectionTable
import org.jetbrains.exposed.sql.ResultRow

object BalanceMapper {

    fun toDomain(row: ResultRow): Balance =
        Balance(
            accountId = row[BalanceProjectionTable.accountId],
            amount = Money.of(row[BalanceProjectionTable.balance]),
            currency = row[BalanceProjectionTable.currency],
            updatedAt = row[BalanceProjectionTable.updatedAt]
        )
}
