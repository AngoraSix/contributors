package com.angorasix.contributors.presentation.dto

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.presentation.dto.A6MediaDto
import com.angorasix.commons.presentation.dto.PatchOperation
import com.angorasix.commons.presentation.dto.PatchOperationSpec
import com.angorasix.contributors.domain.contributor.ContributorMedia
import com.angorasix.contributors.domain.contributor.ProviderUser
import com.angorasix.contributors.domain.contributor.modification.ContributorModification
import com.angorasix.contributors.domain.contributor.modification.UpdateHeadMedia
import com.angorasix.contributors.domain.contributor.modification.UpdateProfileMedia
import com.fasterxml.jackson.databind.ObjectMapper
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
    val profileMedia: A6MediaDto?,
    val headMedia: A6MediaDto?,
    val id: String?,
) : RepresentationModel<ContributorDto>()

enum class SupportedPatchOperations(val op: PatchOperationSpec) {
    REPLACE_HEAD_MEDIA(
        object : PatchOperationSpec {
            override fun supportsPatchOperation(operation: PatchOperation): Boolean =
                operation.op == "replace" && operation.path == "/headMedia"

            override fun mapToObjectModification(
                contributor: SimpleContributor,
                operation: PatchOperation,
                objectMapper: ObjectMapper,
            ): ContributorModification<ContributorMedia> {
                var mediaValue =
                    objectMapper.treeToValue(operation.value, ContributorMedia::class.java)
                return UpdateHeadMedia(mediaValue)
            }
        },
    ),
    REPLACE_PROFILE_MEDIA(
        object : PatchOperationSpec {
            override fun supportsPatchOperation(operation: PatchOperation): Boolean =
                operation.op == "replace" && operation.path == "/profileMedia"

            override fun mapToObjectModification(
                contributor: SimpleContributor,
                operation: PatchOperation,
                objectMapper: ObjectMapper,
            ): ContributorModification<ContributorMedia> {
                var mediaValue =
                    objectMapper.treeToValue(operation.value, ContributorMedia::class.java)
                return UpdateProfileMedia(mediaValue)
            }
        },
    ),
    ;
}
