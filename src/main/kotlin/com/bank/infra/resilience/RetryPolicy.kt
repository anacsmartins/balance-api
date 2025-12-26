package com.bank.infra.resilience

import kotlin.math.pow
import kotlin.random.Random

object RetryPolicy {

    fun <T> execute(
        maxAttempts: Int = 5,
        baseDelayMs: Long = 200,
        block: () -> T
    ): T {
        var attempt = 0

        while (true) {
            try {
                return block()
            } catch (ex: Exception) {
                attempt++
                if (attempt >= maxAttempts) throw ex

                val backoff =
                    baseDelayMs * 2.0.pow(attempt.toDouble()).toLong()

                val jitter = Random.nextLong(0, backoff)
                Thread.sleep(jitter)
            }
        }
    }
}
