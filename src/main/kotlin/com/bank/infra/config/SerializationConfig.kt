package com.bank.infra.config

import io.ktor.server.application.*

//fun Application.configureDatabase() {
//    DatabaseConfig.init(
//        jdbcUrl = environment.config.property("db.jdbcUrl").getString(),
//        user = environment.config.property("db.user").getString(),
//        pass = environment.config.property("db.password").getString()
//    )
//}


fun Application.configureDatabase() {
    val jdbcUrl = System.getenv("DB_JDBC_URL")
        ?: environment.config.property("db.jdbcUrl").getString()

    val user = System.getenv("DB_USER")
        ?: environment.config.property("db.user").getString()

    val password = System.getenv("DB_PASSWORD")
        ?: environment.config.property("db.password").getString()

    DatabaseConfig.init(
        jdbcUrl = jdbcUrl,
        user = user,
        pass = password
    )
}
