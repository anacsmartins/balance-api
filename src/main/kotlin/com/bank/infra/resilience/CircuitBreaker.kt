package com.bank.infra.resilience

class CircuitBreaker(
    private val failureThreshold: Int = 5,
    private val resetTimeoutMs: Long = 30_000
) {

    private var failures = 0
    private var openUntil = 0L

    fun <T> execute(block: () -> T): T {
        val now = System.currentTimeMillis()

        if (now < openUntil) {
            throw IllegalStateException("Circuit breaker OPEN")
        }

        return try {
            val result = block()
            failures = 0
            result
        } catch (ex: Exception) {
            failures++
            if (failures >= failureThreshold) {
                openUntil = now + resetTimeoutMs
            }
            throw ex
        }
    }
}
