package com.bank.infra.persistence.gateway

import com.bank.application.gateway.IdempotencyGateway
import com.bank.infra.persistence.table.IdempotencyTable
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.UUID

class IdempotencyGatewayImpl : IdempotencyGateway {

    override fun tryMarkAsProcessed(transactionId: UUID): Boolean =
        transaction {
            try {
                IdempotencyTable.insert {
                    it[IdempotencyTable.transactionId] = transactionId
                    it[processedAt] = Instant.now()
                }
                true
            } catch (ex: ExposedSQLException) {
                // violação de UNIQUE(transaction_id)
                false
            }
        }
}
