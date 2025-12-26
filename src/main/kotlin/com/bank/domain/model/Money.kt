package com.bank.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

@JvmInline
value class Money private constructor(
    val value: BigDecimal
) {

    companion object {
        private const val SCALE = 2

        fun of(amount: BigDecimal): Money =
            Money(
                amount.setScale(SCALE, RoundingMode.HALF_EVEN)
            )

        fun zero(): Money =
            Money(BigDecimal.ZERO.setScale(SCALE))
    }

    operator fun plus(other: Money): Money =
        of(this.value + other.value)

    operator fun minus(other: Money): Money =
        of(this.value - other.value)
}

