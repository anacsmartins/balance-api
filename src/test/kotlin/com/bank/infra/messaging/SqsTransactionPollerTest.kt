package com.bank.infra.messaging

import com.bank.infra.metrics.IngestionMetrics
import com.bank.infra.serialization.ObjectMapperFactory
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.sqs.SqsClient
import com.fasterxml.jackson.module.kotlin.readValue

class SqsTransactionPollerTest {

    private val sqsClient = mockk<SqsClient>(relaxed = true)
    private val consumer = mockk<SqsTransactionConsumer>(relaxed = true)

    private lateinit var registry: SimpleMeterRegistry
    private lateinit var metrics: IngestionMetrics
    private lateinit var poller: SqsTransactionPoller

    private val queueUrl = "queue-url"
    private val dlqUrl = "dlq-url"

    @BeforeEach
    fun setup() {
        registry = SimpleMeterRegistry()
        metrics = IngestionMetrics(registry)

        poller = SqsTransactionPoller(
            sqsClient = sqsClient,
            queueUrl = queueUrl,
            dlqUrl = dlqUrl,
            consumer = consumer,
            metrics = metrics
        )
    }

    @Test
    fun `should deserialize TransactionEventMessage`() {
        val body = """
            {"transaction":{"id":"9e9ae808-b154-48b5-9f3e-553935cc4543",
            "type":"CREDIT","amount":98.07,"currency":"BRL",
            "status":"APPROVED","timestamp":1751641364589998},
            "account":{"id":"5b19c8b6-0cc4-4c72-a989-0c2ee15fa975",
            "owner":"315e3cfe-f4af-4cd2-b298-a449e614349a","created_at":"1634874339",
            "status":"ENABLED","balance":{"amount":183.12,"currency":"BRL"}}}
    """.trimIndent()

        val mapper = ObjectMapperFactory.instance

        val event = mapper.readValue<TransactionEventMessage>(body)

        kotlin.test.assertNotNull(event)
    }
}

