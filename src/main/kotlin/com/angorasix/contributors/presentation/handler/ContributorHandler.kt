package com.angorasix.contributors.presentation.handler

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.infrastructure.constants.AngoraSixInfrastructure
import com.angorasix.commons.presentation.dto.Patch
import com.angorasix.contributors.application.ContributorService
import com.angorasix.contributors.domain.contributor.Contributor
import com.angorasix.contributors.domain.contributor.ContributorMedia
import com.angorasix.contributors.domain.contributor.ProviderUser
import com.angorasix.contributors.domain.contributor.modification.ContributorModification
import com.angorasix.contributors.infrastructure.security.extractProviderUser
import com.angorasix.contributors.presentation.dto.ContributorDto
import com.angorasix.contributors.presentation.dto.ContributorMediaDto
import com.angorasix.contributors.presentation.dto.SupportedPatchOperations
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.hateoas.MediaTypes
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
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
    fun getAuthenticatedContributor(request: ServerRequest): ServerResponse {
        return request.principalOrNull()?.let {
            val providerUser = extractProviderUser(it)
            val contributor =
                service.findSingleContributor(providerUser)?.convertToDto(providerUser)
            contributor?.let {
                ServerResponse.ok().contentType(MediaTypes.HAL_FORMS_JSON)
                    .body(it)
            }
        } ?: ServerResponse.notFound().build()
    }

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
            ServerResponse.ok().contentType(MediaTypes.HAL_FORMS_JSON)
                .body(it)
        } ?: ServerResponse.notFound().build()
    }

    fun patchContributor(request: ServerRequest): ServerResponse {
        return request.principalOrNull()?.let { principal ->
            val requestingContributor =
                request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY] as SimpleContributor
            val contributorId = request.pathVariable("id")
            val providerUser = extractProviderUser(principal)
            val patch = request.body(Patch::class.java)
            val modifyOperations = patch.operations.map { patchOp ->
                patchOp.toDomainObjectModification(
                    requestingContributor,
                    SupportedPatchOperations.values().map { it.op }.toList(),
                    objectMapper,
                )
            }
            val modifyContributorOperations: List<ContributorModification<Any>> =
                modifyOperations.filterIsInstance<ContributorModification<Any>>()

            val updatedContributor = service.modifyContributor(
                requestingContributor,
                contributorId,
                modifyContributorOperations,
            )?.convertToDto(providerUser)
            updatedContributor?.let {
                return ServerResponse.ok().contentType(MediaTypes.HAL_FORMS_JSON)
                    .body(it)
            }
        } ?: ServerResponse.status(HttpStatus.FORBIDDEN).build()
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
                    service.updateContributor(it, updateContributorData)
                        ?.convertToDto(providerUser)
                contributor?.let {
                    return ServerResponse.ok().contentType(MediaTypes.HAL_FORMS_JSON)
                        .body(it)
                }
            } ?: ServerResponse.status(HttpStatus.FORBIDDEN).build()
        } ?: ServerResponse.status(HttpStatus.FORBIDDEN).build()
    }
}

private fun Contributor.convertToDto(
    requestingProvider: ProviderUser?,
    showAllProviderUsers: Boolean = false,
): ContributorDto {
    val filteredProviderUsers = if (showAllProviderUsers) {
        providerUsers
    } else if (requestingProvider != null && providerUsers.contains(requestingProvider)) {
        setOf(requestingProvider)
    } else {
        emptySet()
    }
    return ContributorDto(
        filteredProviderUsers,
        email,
        firstName,
        lastName,
        profileMedia?.convertToDto(),
        headMedia?.convertToDto(),
        id,
    )
}

private fun ContributorDto.convertToDomain(forProviderUser: ProviderUser): Contributor {
    return Contributor(
        forProviderUser,
        email,
        firstName,
        lastName,
        profileMedia?.convertToDomain(),
        headMedia?.convertToDomain(),
    )
}

private fun ContributorMedia.convertToDto(): ContributorMediaDto {
    return ContributorMediaDto(
        mediaType,
        url,
        thumbnailUrl,
        resourceId,
    )
}

private fun ContributorMediaDto.convertToDomain(): ContributorMedia {
    return ContributorMedia(
        mediaType,
        url,
        thumbnailUrl,
        resourceId,
    )
}
