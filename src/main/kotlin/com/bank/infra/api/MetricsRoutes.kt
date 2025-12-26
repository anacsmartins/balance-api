package com.bank.infra.api

import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Route.metricsRoutes(registry: PrometheusMeterRegistry) {
    get("/metrics") {
        call.respondText(registry.scrape())
    }
    get("/metrics/debug") {
        call.respond(
            mapOf(
                "sqs" to mapOf(
                    "consumed" to registry.counter("sqs_messages_consumed_total").count(),
                    "processed" to registry.counter("sqs_messages_processed_total").count(),
                    "dlq" to registry.counter("sqs_messages_dlq_sent_total").count(),
                    "schema_error" to registry.counter("sqs_messages_schema_error_total").count(),
                    "unclassifiedFailures" to registry.counter("sqs_messages_unclassified_failure_total").count(),
                    "inflight" to registry.counter("sqs_messages_inflight_total").count(),
                )
            )
        )
    }

}

