package com.bank.infra.api.response

import com.bank.domain.model.Balance
import java.time.ZoneOffset

object BalanceResponseMapper {

    fun fromDomain(balance: Balance): BalanceResponse =
        BalanceResponse(
            accountId = balance.accountId,
            amount = balance.amount.value,
            currency = balance.currency,
            updatedAt = balance.updatedAt
                .atZone(ZoneOffset.UTC)
                .toInstant()
        )
}
