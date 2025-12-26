package com.bank.infra.messaging

import kotlinx.coroutines.*

class SqsConsumerRunner(
    private val poller: SqsTransactionPoller
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start() {
        scope.launch {
            while (isActive) {
                poller.pollOnce()
                delay(1_000)
            }
        }
    }

    fun stop() {
        scope.cancel()
    }
}
