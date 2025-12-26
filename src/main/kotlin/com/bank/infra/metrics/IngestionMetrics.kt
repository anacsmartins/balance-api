package com.bank.infra.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Gauge
import java.util.concurrent.atomic.AtomicInteger

    class IngestionMetrics(registry: MeterRegistry) {
        val consumed: Counter =
             Counter.builder("sqs_messages_consumed_total").register(registry)

        val processed: Counter =
            Counter.builder("sqs_messages_processed_total").register(registry)

        val schemaErrors: Counter =
            Counter.builder("sqs_messages_schema_error_total").register(registry)

        val dlqSent: Counter =
            Counter.builder("sqs_messages_dlq_sent_total").register(registry)

        val transientErrors: Counter =
            Counter.builder("sqs_messages_transient_error_total").register(registry)

        val unclassifiedFailures: Counter =
            Counter.builder("sqs_messages_unclassified_failure_total")
                .description("Messages that failed without a final classification")
                .register(registry)

        private val inflightMessages = AtomicInteger(0)

        val inflight: AtomicInteger = inflightMessages.also {
            Gauge.builder("sqs_messages_inflight", it::get)
                .description("Messages currently being processed (in-flight)")
                .register(registry)
    }
}
