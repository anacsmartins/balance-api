package com.bank.infra.api

import io.ktor.server.routing.*

fun Route.balanceRoutes(balanceController: BalanceController) {

    route("/accounts") {
        get("/{accountId}/balance") {
            balanceController.getBalance(call)
        }
    }
}
