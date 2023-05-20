package com.angorasix.contributors.presentation.router

import com.angorasix.contributors.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.contributors.presentation.handler.ContributorHandler
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.servlet.function.RouterFunction
import org.springframework.web.servlet.function.router

/**
 * Router for all Contributor related endpoints.
 *
 * @author rozagerardo
 */
class ContributorRouter(
    private val handler: ContributorHandler,
    private val objectMapper: ObjectMapper,
    private val apiConfigs: ApiConfigs,
) {

    /**
     * Main RouterFunction configuration for all endpoints related to Contributors.
     *
     * @return the [RouterFunction] with all the routes for Contributors
     */
    fun projectRouterFunction() = router {
        apiConfigs.basePaths.contributor.nest {
            apiConfigs.routes.baseCrudRoute.nest {
                method(apiConfigs.routes.getAuthenticatedContributor.method).nest {
                    method(
                        apiConfigs.routes.getAuthenticatedContributor.method,
                        handler::getAuthenticatedContributor,
                    )
                }
            }
        }
    }
}
