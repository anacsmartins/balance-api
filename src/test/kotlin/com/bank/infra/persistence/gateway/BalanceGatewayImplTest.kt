package com.bank.infra.persistence.gateway

import com.bank.domain.model.Balance
import com.bank.domain.model.Money
import com.bank.infra.persistence.BaseExposedTest
import com.bank.infra.persistence.table.BalanceProjectionTable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BalanceGatewayImplTest : BaseExposedTest() {

    private val gateway = BalanceGatewayImpl()
    private val accountId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        createSchema(BalanceProjectionTable)
        clear(BalanceProjectionTable)
    }

    @Test
    fun `should keep the most recent balance snapshot (last-write-wins)`() {
        val olderSnapshot = Balance(
            accountId = accountId,
            amount = Money.of(BigDecimal("100.00")),
            currency = "BRL",
            updatedAt = Instant.parse("2024-01-01T10:00:00Z")
        )

        val newerSnapshot = Balance(
            accountId = accountId,
            amount = Money.of(BigDecimal("250.00")),
            currency = "BRL",
            updatedAt = Instant.parse("2024-01-01T12:00:00Z")
        )

        gateway.upsertSnapshot(newerSnapshot)
        gateway.upsertSnapshot(olderSnapshot)

        val result = gateway.findByAccountId(accountId)

        assertNotNull(result)
        assertEquals(BigDecimal("250.00"), result.amount.value)
        assertEquals(newerSnapshot.updatedAt, result.updatedAt)
    }
}
