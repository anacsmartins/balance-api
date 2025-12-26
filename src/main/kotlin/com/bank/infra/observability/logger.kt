package com.bank.infra.observability

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *   private val log = logger<MyClass>()
 */
inline fun <reified T> logger(): Logger =
    LoggerFactory.getLogger(T::class.java)

