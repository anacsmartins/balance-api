package com.bank.infra.config

import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.micrometer.prometheus.PrometheusConfig

fun Application.configureMetrics(
    registry: PrometheusMeterRegistry
) {
    install(MicrometerMetrics) {
        this.registry = registry
    }
}
