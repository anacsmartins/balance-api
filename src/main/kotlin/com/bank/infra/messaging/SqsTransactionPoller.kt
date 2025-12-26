package com.bank.infra.messaging

import com.bank.infra.metrics.IngestionMetrics
import com.bank.infra.resilience.CircuitBreaker
import com.bank.infra.resilience.RetryPolicy
import com.bank.infra.serialization.ObjectMapperFactory
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.readValue
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import com.bank.infra.observability.LogContext
import com.bank.infra.observability.logger
import com.fasterxml.jackson.databind.exc.ValueInstantiationException

class SqsTransactionPoller(
    private val sqsClient: SqsClient,
    private val queueUrl: String,
    private val dlqUrl: String,
    private val consumer: SqsTransactionConsumer,
    private val metrics: IngestionMetrics
) {

    private val mapper = ObjectMapperFactory.instance
    private val circuitBreaker = CircuitBreaker()
    private val log = logger<SqsTransactionPoller>()

    fun pollOnce() {
        val response = sqsClient.receiveMessage(
            ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(10)
                .build()
        )

        log.info("Polled SQS. Messages received: ${response.messages().size}")

        response.messages().forEach { msg ->
            processMessage(msg)
        }
    }

    private fun processMessage(msg: Message) {
        LogContext.withMessage(msg.messageId()) {
            metrics.inflight.incrementAndGet()
            try {
                metrics.consumed.increment()

                val event: TransactionEventMessage =
                    mapper.readValue(msg.body())

                RetryPolicy.execute {
                    circuitBreaker.execute {
                        consumer.handle(event)
                        metrics.processed.increment()
                    }
                }

                deleteFromMainQueue(msg)

                log.info("Message processed successfully")
            } catch (ex: ValueInstantiationException) {
                // Erro de domínio ocorrido DURANTE desserialização
                log.error(
                    "Invalid domain value during deserialization. Sending to DLQ. Body=${msg.body()}",
                    ex
                )

                sendToDlq(
                    msg,
                    reason = "INVALID_DOMAIN: ${ex.cause?.message ?: ex.message}"
                )

                metrics.schemaErrors.increment()
                metrics.dlqSent.increment()

                deleteFromMainQueue(msg)

            } catch (ex: MismatchedInputException) {
                metrics.schemaErrors.increment()
                metrics.dlqSent.increment()
                sendToDlq(msg, "INVALID_SCHEMA: ${ex.message}")
                deleteFromMainQueue(msg)

            } catch (ex: IllegalArgumentException) {
                metrics.schemaErrors.increment()
                metrics.dlqSent.increment()
                sendToDlq(msg, "INVALID_DOMAIN: ${ex.message}")
                deleteFromMainQueue(msg)

            } catch (ex: Exception) {
                metrics.unclassifiedFailures.increment()
                metrics.transientErrors.increment()
                throw ex

            } finally {
                metrics.inflight.decrementAndGet()
            }
        }
    }


    private fun deleteFromMainQueue(msg: Message) {
        sqsClient.deleteMessage {
            it.queueUrl(queueUrl)
            it.receiptHandle(msg.receiptHandle())
        }
    }

    private fun sendToDlq(msg: Message, reason: String) {
        sqsClient.sendMessage {
            it.queueUrl(dlqUrl)
            it.messageBody(msg.body())
            it.messageAttributes(
                mapOf(
                    "dlq_reason" to MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(reason)
                        .build(),
                    "original_message_id" to MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(msg.messageId())
                        .build(),
                    "source_queue" to MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(queueUrl)
                        .build()
                )
            )
        }

        log.warn(
            "Message sent to DLQ | messageId=${msg.messageId()} | reason=$reason"
        )
    }
}
