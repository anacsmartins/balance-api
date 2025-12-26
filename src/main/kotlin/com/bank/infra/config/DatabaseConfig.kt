package com.bank.infra.config

import com.bank.infra.persistence.table.BalanceProjectionTable
import com.bank.infra.persistence.table.IdempotencyTable
import com.bank.infra.persistence.table.TransactionTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseConfig {

    fun init(
        jdbcUrl: String,
        user: String,
        pass: String
    ) {
        val hikariConfig = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = user
            this.password = pass
            this.driverClassName = "org.postgresql.Driver"
            this.maximumPoolSize = 10
            this.isAutoCommit = false
            this.transactionIsolation = "TRANSACTION_READ_COMMITTED"
        }

        val dataSource = HikariDataSource(hikariConfig)
        println("isRunning: " + dataSource.isRunning.toString() +" isReadOnly: "+ dataSource.isReadOnly.toString()
                + " isClosed: " + dataSource.isClosed.toString())
        Database.connect(datasource = dataSource)

        if (System.getenv("APP_ENV") != "prod") {
            transaction {
                SchemaUtils.create(
                    TransactionTable,
                    BalanceProjectionTable,
                    IdempotencyTable
                )
            }
        }
    }
}
