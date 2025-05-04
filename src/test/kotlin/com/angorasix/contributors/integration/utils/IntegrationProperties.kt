package com.angorasix.contributors.integration.utils

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 *
 *
 * @author rozagerardo
 */
@ConfigurationProperties("integration")
class IntegrationProperties(
    val mongodb: MongodbProperties,
) {
    data class MongodbProperties(
        val baseJsonFile: String,
    )
}
