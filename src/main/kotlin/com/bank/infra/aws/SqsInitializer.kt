package com.bank.infra.aws

import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException

class SqsInitializer(
    private val sqsClient: SqsClient
) {

    fun ensureQueueExists(queueName: String): String {
        return try {
            sqsClient.getQueueUrl {
                it.queueName(queueName)
            }.queueUrl()
        } catch (ex: QueueDoesNotExistException) {
            sqsClient.createQueue {
                it.queueName(queueName)
            }.queueUrl()
        }
    }
}
