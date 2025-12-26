package com.bank.domain.enums

import com.fasterxml.jackson.annotation.JsonCreator

enum class TransactionStatus {
    AUTHORIZED,
    DECLINED;

    companion object {

        @JsonCreator
        @JvmStatic
        fun from(value: String): TransactionStatus =
            when (value.trim().uppercase()) {
                "AUTHORIZED" -> AUTHORIZED
                "APPROVED"   -> AUTHORIZED
                "DECLINED"   -> DECLINED
                else -> throw IllegalArgumentException(
                    "Invalid TransactionStatus: $value"
                )
            }
    }
}
