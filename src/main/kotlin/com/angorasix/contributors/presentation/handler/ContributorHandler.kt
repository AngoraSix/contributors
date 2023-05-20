package com.angorasix.contributors.presentation.handler

import com.angorasix.contributors.application.ContributorService
import com.angorasix.contributors.domain.contributor.Contributor
import com.angorasix.contributors.domain.contributor.ContributorMedia
import com.angorasix.contributors.domain.contributor.ProviderUser
import com.angorasix.contributors.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.contributors.infrastructure.security.extractProviderUser
import com.angorasix.contributors.presentation.dto.ContributorDto
import com.angorasix.contributors.presentation.dto.ContributorMediaDto
import org.springframework.hateoas.MediaTypes
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
    private val apiConfigs: ApiConfigs,
) {

    /**
     * Handler for the Get Single Contributor endpoint,
     * retrieving a Mono with the requested Contributor.
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
}

private fun Contributor.convertToDto(forProvider: ProviderUser?): ContributorDto {
    val filteredProviderUsers = forProvider?.let {
        if (!providerUsers.contains(it)) {
            throw IllegalArgumentException("Can't access contributor for provider ${forProvider.issuer}")
        }
        setOf(forProvider)
    } ?: providerUsers
    return ContributorDto(
        filteredProviderUsers,
        email,
        firstName,
        lastName,
        profileMedia?.convertToDto(),
        headMedia?.convertToDto(),
        id
    )
}


//private fun Contributor.convertToDto(
//    requestingContributor: RequestingContributor?,
//    apiConfigs: ApiConfigs,
//    request: ServerRequest,
//): ContributorDto =
//    convertToDto().resolveHypermedia(requestingContributor, apiConfigs, request)

//private fun ContributorDto.convertToDomain(): Contributor {
//    return Contributor(
//        projectId ?: throw IllegalArgumentException("ProjectPresentation projectId expected"),
//        referenceName ?: throw IllegalArgumentException(
//            "ProjectPresentation referenceName expected",
//        ),
//        sections?.map { it.convertToDomain() }?.toMutableSet(),
//    )
//}
private fun ContributorMedia.convertToDto(): ContributorMediaDto {
    return ContributorMediaDto(
        mediaType,
        url,
        thumbnailUrl,
        resourceId,
    )
}