package com.bank.infra.persistence

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll

abstract class BaseExposedTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initDatabase() {
            Database.connect(
                url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
                driver = "org.h2.Driver"
            )
        }
    }

    protected fun createSchema(vararg tables: org.jetbrains.exposed.sql.Table) {
        transaction {
            SchemaUtils.create(*tables)
        }
    }

    protected fun clear(vararg tables: org.jetbrains.exposed.sql.Table) {
        transaction {
            tables.forEach { it.deleteAll() }
        }
    }
}
