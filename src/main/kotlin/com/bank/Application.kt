package com.bank

import com.bank.infra.api.balanceRoutes
import com.bank.application.wiring.ApplicationWiring
import com.bank.infra.api.BalanceController
import com.bank.infra.api.metricsRoutes
import com.bank.infra.aws.SqsClientFactory
import com.bank.infra.aws.SqsInitializer
import com.bank.infra.config.configureDatabase
import com.bank.infra.config.configureMetrics
import com.bank.infra.messaging.*
import com.bank.infra.metrics.IngestionMetrics
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Application.module() {

    val isTest = environment.config.propertyOrNull("ktor.environment")
        ?.getString() == "test"

    if (!isTest) {
        configureDatabase()
    }

    // ----------------------------
    // Metrics
    // ----------------------------
    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    configureMetrics(registry)
    val ingestionMetrics = IngestionMetrics(registry)


    // ----------------------------
    // Ingestão (SQS)
    // ----------------------------
    val ingestTransaction = ApplicationWiring.ingestTransaction()
    val consumer = SqsTransactionConsumer(ingestTransaction)

    val sqsClient = SqsClientFactory.create()
    val sqsInitializer = SqsInitializer(sqsClient)

    val queueUrl = sqsInitializer.ensureQueueExists("transacoes-financeiras-processadas")
    val dlqUrl = sqsInitializer.ensureQueueExists("transacoes-financeiras-dlq")

    val poller = SqsTransactionPoller(
        sqsClient = sqsClient,
        queueUrl = queueUrl,
        dlqUrl = dlqUrl,
        consumer = consumer,
        metrics = ingestionMetrics
    )

    val runner = SqsConsumerRunner(poller)
    runner.start()
    log.info("SQS Transaction Consumer started")

    environment.monitor.subscribe(ApplicationStopping) {
        log.info("Stopping SQS Transaction Consumer")
        runner.stop()
    }

    // ----------------------------
    // Exposição (HTTP API)
    // ----------------------------
    install(ContentNegotiation) {
        jackson {
            findAndRegisterModules()
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }

    val balanceGateway = ApplicationWiring.balanceGateway()
    val balanceController = BalanceController(balanceGateway)

    routing {
        balanceRoutes(balanceController)
        metricsRoutes(registry)
    }
}
