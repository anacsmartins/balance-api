package com.bank.application.gateway

import com.bank.domain.model.Balance
import java.util.UUID

interface BalanceGateway {
    fun upsertSnapshot(balance: Balance)
    fun findByAccountId(accountId: UUID): Balance?
}

