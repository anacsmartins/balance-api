package com.bank.infra.aws

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient
import java.net.URI

object SqsClientFactory {

    fun create(): SqsClient {
        val endpoint = System.getenv("AWS_ENDPOINT") ?: error(
            "AWS_ENDPOINT not defined"
        )

        val region = System.getenv("AWS_REGION") ?: "sa-east-1"

        return SqsClient.builder()
            .endpointOverride(URI.create(endpoint))
            .region(Region.of(region))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        System.getenv("AWS_ACCESS_KEY_ID") ?: "test",
                        System.getenv("AWS_SECRET_ACCESS_KEY") ?: "test"
                    )
                )
            )
            .build()
    }
}
