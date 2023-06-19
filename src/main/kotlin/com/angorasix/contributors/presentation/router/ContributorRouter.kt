package com.angorasix.contributors.presentation.router

import com.angorasix.contributors.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.contributors.presentation.filters.extractServletRequestingContributor
import com.angorasix.contributors.presentation.handler.ContributorHandler
import org.springframework.web.servlet.function.RouterFunction
import org.springframework.web.servlet.function.router

/**
 * Router for all Contributor related endpoints.
 *
 * @author rozagerardo
 */
class ContributorRouter(
    private val handler: ContributorHandler,
    private val apiConfigs: ApiConfigs,
) {

    /**
     * Main RouterFunction configuration for all endpoints related to Contributors.
     *
     * @return the [RouterFunction] with all the routes for Contributors
     */
    fun projectRouterFunction() = router {
        apiConfigs.basePaths.contributor.nest {
            filter { request, next ->
                extractServletRequestingContributor(
                    request,
                    next,
                )
            }
            apiConfigs.routes.baseByIdCrudRoute.nest {
                method(apiConfigs.routes.getContributor.method).nest {
                    method(
                        apiConfigs.routes.getContributor.method,
                        handler::getContributor,
                    )
                }
                method(apiConfigs.routes.patchContributor.method).nest {
                    method(
                        apiConfigs.routes.patchContributor.method,
                        handler::patchContributor,
                    )
                }
            }
        }
    }
}
