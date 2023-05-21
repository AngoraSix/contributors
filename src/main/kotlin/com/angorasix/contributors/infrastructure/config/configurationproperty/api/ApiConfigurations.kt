package com.angorasix.contributors.infrastructure.config.configurationproperty.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod

/**
 * <p>
 *  Base file containing all Service configurations.
 * </p>
 *
 * @author rozagerardo
 */
@ConfigurationProperties(prefix = "configs.api")
data class ApiConfigs(
    @NestedConfigurationProperty
    var headers: HeadersConfigs,
    @NestedConfigurationProperty
    var routes: RoutesConfigs,
    @NestedConfigurationProperty
    var basePaths: BasePathConfigs
)

data class HeadersConfigs constructor(val contributor: String)

data class BasePathConfigs constructor(val contributor: String)

data class RoutesConfigs constructor(
    val baseCrudRoute: String,
    val getAuthenticatedContributor: Route
//    val baseListCrudRoute: String,
//    val baseByIdCrudRoute: String,
//    val createContributor: Route,
//    val updateContributor: Route,
//    val getContributor: Route,
//    val listContributors: Route,
)

data class Route(
    val name: String,
    val basePaths: List<String>,
    val method: HttpMethod,
    val path: String,
) {
    fun resolvePath(): String = basePaths.joinToString("").plus(path)
}