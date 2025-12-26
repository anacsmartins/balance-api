package com.bank.infra.observability

import org.slf4j.MDC

object LogContext {

    fun withTransaction(
        transactionId: String,
        accountId: String,
        block: () -> Unit
    ) {
        try {
            MDC.put("transactionId", transactionId)
            MDC.put("accountId", accountId)
            block()
        } finally {
            MDC.clear()
        }
    }

    fun withMessage(
        messageId: String,
        block: () -> Unit
    ) {
        try {
            MDC.put("sqsMessageId", messageId)
            block()
        } finally {
            MDC.remove("sqsMessageId")
        }
    }
}
