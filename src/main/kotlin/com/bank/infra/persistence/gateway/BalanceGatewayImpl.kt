package com.bank.infra.persistence.gateway

import com.bank.application.gateway.BalanceGateway
import com.bank.domain.model.Balance
import com.bank.infra.persistence.mapper.BalanceMapper
import com.bank.infra.persistence.table.BalanceProjectionTable
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class BalanceGatewayImpl : BalanceGateway {

    override fun findByAccountId(accountId: UUID): Balance? =
        transaction {
            BalanceProjectionTable
                .select { BalanceProjectionTable.accountId eq accountId }
                .limit(1)
                .map(BalanceMapper::toDomain)
                .singleOrNull()
        }

//    override fun upsertSnapshot(balance: Balance) {
//        transaction {
//
//            val updated = BalanceProjectionTable.update(
//                where = {
//                    (BalanceProjectionTable.accountId eq balance.accountId) and
//                            (BalanceProjectionTable.updatedAt less balance.updatedAt)
//                }
//            ) {
//                it[BalanceProjectionTable.balance] = balance.amount.value
//                it[BalanceProjectionTable.currency] = balance.currency
//                it[BalanceProjectionTable.updatedAt] = balance.updatedAt
//            }
//
//            if (updated == 0) {
//                BalanceProjectionTable.insertIgnore {
//                    it[accountId] = balance.accountId
//                    it[BalanceProjectionTable.balance] = balance.amount.value
//                    it[BalanceProjectionTable.currency] = balance.currency
//                    it[updatedAt] = balance.updatedAt
//                }
//            }
//        }
//    }

    override fun upsertSnapshot(balance: Balance) {
        transaction {

            val existing = BalanceProjectionTable
                .select { BalanceProjectionTable.accountId eq balance.accountId }
                .forUpdate()
                .singleOrNull()

            if (existing == null) {
                BalanceProjectionTable.insert {
                    it[accountId] = balance.accountId
                    it[BalanceProjectionTable.balance] = balance.amount.value
                    it[BalanceProjectionTable.currency] = balance.currency
                    it[updatedAt] = balance.updatedAt
                }
            } else {
                val currentUpdatedAt = existing[BalanceProjectionTable.updatedAt]

                if (currentUpdatedAt < balance.updatedAt) {
                    BalanceProjectionTable.update(
                        where = { BalanceProjectionTable.accountId eq balance.accountId }
                    ) {
                        it[BalanceProjectionTable.balance] = balance.amount.value
                        it[BalanceProjectionTable.currency] = balance.currency
                        it[updatedAt] = balance.updatedAt
                    }
                }
            }
        }
    }


}
