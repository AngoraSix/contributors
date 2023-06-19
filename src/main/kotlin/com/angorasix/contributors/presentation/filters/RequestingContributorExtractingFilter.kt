package com.angorasix.contributors.presentation.filters

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.infrastructure.constants.AngoraSixInfrastructure
import com.angorasix.commons.infrastructure.oauth2.constants.A6WellKnownClaims
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.principalOrNull

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
fun extractServletRequestingContributor(
    request: ServerRequest,
    next: (ServerRequest) -> ServerResponse,
): ServerResponse {
    val authentication = request.principalOrNull() as JwtAuthenticationToken?
    val jwtPrincipal = authentication?.token
    jwtPrincipal?.let {
        val requestingContributor =
            SimpleContributor(
                it.getClaim(A6WellKnownClaims.CONTRIBUTOR_ID),
                authentication.authorities.map { it.authority }.toSet(),
            )
        request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY] =
            requestingContributor
    }
    return next(request)
}
