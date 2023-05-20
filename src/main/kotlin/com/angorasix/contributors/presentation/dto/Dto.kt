package com.angorasix.contributors.presentation.dto

import com.angorasix.contributors.domain.contributor.ProviderUser
import org.springframework.hateoas.RepresentationModel

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
data class ContributorDto(
    val providerUsers: Set<ProviderUser>,
    var email: String?,
    var firstName: String?,
    var lastName: String?,
    val profileMedia: ContributorMediaDto?,
    val headMedia: ContributorMediaDto?,
    val id: String?,
) : RepresentationModel<ContributorDto>()

data class ContributorMediaDto(
    val mediaType: String,
    val url: String,
    val thumbnailUrl: String,
    val resourceId: String,
)