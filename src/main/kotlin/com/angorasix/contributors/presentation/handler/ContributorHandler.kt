package com.angorasix.contributors.presentation.handler

import com.angorasix.commons.domain.A6Contributor
import com.angorasix.commons.infrastructure.constants.AngoraSixInfrastructure
import com.angorasix.commons.presentation.dto.A6MediaDto
import com.angorasix.commons.presentation.dto.Patch
import com.angorasix.commons.presentation.handler.convertToDto
import com.angorasix.commons.servlet.presentation.error.resolveNotFound
import com.angorasix.commons.servlet.presentation.error.resolveUnauthorized
import com.angorasix.contributors.application.ContributorService
import com.angorasix.contributors.domain.contributor.Contributor
import com.angorasix.contributors.domain.contributor.ContributorMedia
import com.angorasix.contributors.domain.contributor.ProviderUser
import com.angorasix.contributors.domain.contributor.modification.ContributorModification
import com.angorasix.contributors.infrastructure.queryfilters.ListContributorsFilter
import com.angorasix.contributors.infrastructure.security.extractProviderUser
import com.angorasix.contributors.presentation.dto.ContributorDto
import com.angorasix.contributors.presentation.dto.ContributorQueryParams
import com.angorasix.contributors.presentation.dto.SupportedPatchOperations
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.hateoas.MediaTypes
import org.springframework.util.MultiValueMap
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.ServerResponse.ok
import org.springframework.web.servlet.function.principalOrNull

/**
 * Contributor Handler (Controller) containing all handler functions related to Contributor endpoints.
 *
 * @author rozagerardo
 */
class ContributorHandler(
    private val service: ContributorService,
    private val objectMapper: ObjectMapper,
) {
    /**
     * Handler for the Get Single Contributor (Authenticated) endpoint,
     * retrieving the authenticated Contributor.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    fun getAuthenticatedContributor(request: ServerRequest): ServerResponse =
        request.principalOrNull()?.let {
            val providerUser = extractProviderUser(it)
            val contributor =
                service.findSingleContributor(providerUser)?.convertToDto(providerUser)
            contributor?.let {
                ServerResponse
                    .ok()
                    .contentType(MediaTypes.HAL_FORMS_JSON)
                    .body(it)
            }
        } ?: resolveNotFound("Can't find Contributor", "Contributor")

    /**
     * Handler for the Get Single Contributor (Authenticated) endpoint,
     * retrieving the authenticated Contributor.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    fun getContributor(request: ServerRequest): ServerResponse {
        val contributorId = request.pathVariable("id")
        val requestingProviderUser = request.principalOrNull()?.let { extractProviderUser(it) }
        val contributor =
            service.findSingleContributor(contributorId)?.convertToDto(requestingProviderUser)
        return contributor?.let {
            ServerResponse
                .ok()
                .contentType(MediaTypes.HAL_FORMS_JSON)
                .body(it)
        } ?: resolveNotFound("Can't find Contributor", "Contributor")
    }

    fun patchContributor(request: ServerRequest): ServerResponse {
        return request.principalOrNull()?.let { principal ->
            val requestingContributor =
                request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY] as A6Contributor
            val contributorId = request.pathVariable("id")
            val providerUser = extractProviderUser(principal)
            val patch = request.body(Patch::class.java)
            val modifyOperations =
                patch.operations.map { patchOp ->
                    patchOp.toDomainObjectModification(
                        requestingContributor,
                        SupportedPatchOperations.values().map { it.op }.toList(),
                        objectMapper,
                    )
                }
            val modifyContributorOperations: List<ContributorModification<Any>> =
                modifyOperations.filterIsInstance<ContributorModification<Any>>()

            val updatedContributor =
                service
                    .modifyContributor(
                        requestingContributor,
                        contributorId,
                        modifyContributorOperations,
                    )?.convertToDto(providerUser)
            updatedContributor?.let {
                return ServerResponse
                    .ok()
                    .contentType(MediaTypes.HAL_FORMS_JSON)
                    .body(it)
            }
        } ?: resolveUnauthorized(
            "Patch Contributor endpoint can't determine authentication principal",
            "PRINCIPAL",
        )
    }

    /**
     * Handler for the Update Authenticated Contributor endpoint.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    fun updateContributor(request: ServerRequest): ServerResponse {
        return request.principalOrNull()?.let { principal ->
            val contributorId = request.pathVariable("id")
            val providerUser = extractProviderUser(principal)
            val contributorToUpdate = service.checkContributor(contributorId, providerUser)
            contributorToUpdate?.let {
                val updateContributorData =
                    request.body(ContributorDto::class.java).convertToDomain(providerUser)
                val contributor =
                    service
                        .updateContributor(it, updateContributorData)
                        ?.convertToDto(providerUser)
                contributor?.let {
                    return ok()
                        .contentType(MediaTypes.HAL_FORMS_JSON)
                        .body(it)
                }
            } ?: resolveUnauthorized(
                "Update Contributor endpoint can't determine authentication principal",
                "PRINCIPAL",
            )
        } ?: resolveUnauthorized(
            "Update Contributor endpoint can't determine authentication principal",
            "PRINCIPAL",
        )
    }

    /**
     * Handler for the List Contributors endpoint.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    fun listContributors(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]
        return service
            .findContributors(request.params().toQueryFilter())
            .map {
                it.convertToDto(requestingContributor as? A6Contributor)
            }.let {
                ok().contentType(MediaTypes.HAL_FORMS_JSON).body(it)
            }
    }
}

private fun Contributor.convertToDto(
    requestingProvider: ProviderUser?,
    showAllProviderUsers: Boolean = false,
): ContributorDto {
    val filteredProviderUsers =
        if (showAllProviderUsers) {
            providerUsers
        } else if (requestingProvider != null && providerUsers.contains(requestingProvider)) {
            setOf(requestingProvider)
        } else {
            emptySet()
        }
    return ContributorDto(
        filteredProviderUsers,
        null,
        firstName,
        lastName,
        profileMedia?.convertToDto(),
        headMedia?.convertToDto(),
        id,
    )
}

private fun Contributor.convertToDto(
    requestingContributor: A6Contributor?,
    showAllProviderUsers: Boolean = false,
): ContributorDto {
    val isRequestingContributor = requestingContributor?.contributorId == id
    val filteredProviderUsers =
        if (isRequestingContributor && showAllProviderUsers) {
            providerUsers
        } else {
            emptySet()
        }
    return ContributorDto(
        filteredProviderUsers,
        if (isRequestingContributor) email else null,
        firstName,
        lastName,
        profileMedia?.convertToDto(),
        headMedia?.convertToDto(),
        id,
    )
}

private fun ContributorDto.convertToDomain(forProviderUser: ProviderUser): Contributor =
    Contributor(
        forProviderUser,
        email,
        firstName,
        lastName,
        profileMedia?.convertToDomain(),
        headMedia?.convertToDomain(),
    )

private fun A6MediaDto.convertToDomain(): ContributorMedia =
    ContributorMedia(
        mediaType,
        url,
        thumbnailUrl,
        resourceId,
    )

private fun MultiValueMap<String, String>.toQueryFilter(): ListContributorsFilter =
    ListContributorsFilter(
        id =
            get(ContributorQueryParams.IDS.param)?.flatMap {
                it.split(
                    ",",
                )
            },
    )
