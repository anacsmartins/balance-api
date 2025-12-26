package com.bank.application.usecase

import com.bank.application.gateway.BalanceGateway
import com.bank.application.gateway.IdempotencyGateway
import com.bank.application.gateway.TransactionGateway
import com.bank.domain.enums.TransactionStatus
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class IngestTransactionTest {

    private val transactionGateway = mockk<TransactionGateway>(relaxed = true)
    private val balanceGateway = mockk<BalanceGateway>(relaxed = true)
    private val idempotencyGateway = mockk<IdempotencyGateway>()

    private lateinit var useCase: IngestTransaction

    @BeforeEach
    fun setup() {
        useCase = IngestTransaction(
            transactionGateway,
            balanceGateway,
            idempotencyGateway
        )
    }

    @Test
    fun `should ingest transaction successfully when not processed`() {
        // given
        val command = sampleCommand()

        every { idempotencyGateway.tryMarkAsProcessed(command.transactionId) } returns true

        // when
        useCase.execute(command)

        // then
        verify(exactly = 1) { transactionGateway.save(any()) }
        verify(exactly = 1) { balanceGateway.upsertSnapshot(any()) }
    }

    @Test
    fun `should not ingest transaction when already processed`() {
        // given
        val command = sampleCommand()

        every { idempotencyGateway.tryMarkAsProcessed(command.transactionId) } returns false

        // when
        useCase.execute(command)

        // then
        verify(exactly = 0) { transactionGateway.save(any()) }
        verify(exactly = 0) { balanceGateway.upsertSnapshot(any()) }
    }

    @Test
    fun `should propagate exception when transaction gateway fails`() {
        // given
        val command = sampleCommand()

        every { idempotencyGateway.tryMarkAsProcessed(command.transactionId) } returns true
        every { transactionGateway.save(any()) } throws RuntimeException("DB error")

        // when / then
        try {
            useCase.execute(command)
        } catch (ex: RuntimeException) {
            // expected
        }

        verify(exactly = 1) { transactionGateway.save(any()) }
        verify(exactly = 0) { balanceGateway.upsertSnapshot(any()) }
    }

    // ------------------------
    // helpers
    // ------------------------

    private fun sampleCommand() =
        IngestTransactionCommand(
            transactionId = UUID.randomUUID(),
            accountId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            status = TransactionStatus.AUTHORIZED,
            occurredAt = Instant.now(),
            reportedAccountBalance = BigDecimal("500.00"),
            reportedAccountCurrency = "BRL"
        )
}
