package com.bank.infra.messaging

import com.bank.application.usecase.IngestTransaction

class SqsTransactionConsumer(
    private val ingestTransaction: IngestTransaction
) {
    fun handle(message: TransactionEventMessage) {
        val command = TransactionMessageMapper.from(message)
        ingestTransaction.execute(command)
    }
}
