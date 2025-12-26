package com.bank.domain.enums

import com.fasterxml.jackson.annotation.JsonCreator

enum class AccountStatus {
    ENABLED,
    DISABLED,
    BLOCKED;

    companion object {

        @JsonCreator
        @JvmStatic
        fun from(value: String): AccountStatus =
            entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid AccountStatus: $value")
    }
}

