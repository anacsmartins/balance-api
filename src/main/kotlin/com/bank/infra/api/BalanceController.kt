package com.bank.infra.api

import com.bank.application.gateway.BalanceGateway
import com.bank.infra.api.response.BalanceResponse
import com.bank.infra.api.response.BalanceResponseMapper
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import java.util.UUID

class BalanceController(
    private val balanceGateway: BalanceGateway
) {

    suspend fun getBalance(call: ApplicationCall) {
        val accountId = runCatching {
            UUID.fromString(call.parameters["accountId"])
        }.getOrElse {
            call.respond(HttpStatusCode.BadRequest, "Invalid accountId")
            return
        }

        val balance = balanceGateway.findByAccountId(accountId)
            ?: run {
                call.respond(HttpStatusCode.NotFound, "Account not found")
                return
            }

        call.respond(HttpStatusCode.OK, BalanceResponseMapper.fromDomain(balance))
    }
}
