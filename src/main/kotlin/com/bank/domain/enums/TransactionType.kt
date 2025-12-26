package com.bank.domain.enums

import com.fasterxml.jackson.annotation.JsonCreator

enum class TransactionType {
    CREDIT,
    DEBIT;

    companion object {
        @JsonCreator
        @JvmStatic
        fun from(value: String): TransactionType =
            entries.firstOrNull { it.name.equals(value, true) }
                ?: throw IllegalArgumentException("Invalid TransactionType: $value")
    }
}