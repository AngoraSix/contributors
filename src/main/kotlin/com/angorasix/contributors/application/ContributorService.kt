package com.angorasix.contributors.application

import com.angorasix.contributors.domain.contributor.Contributor
import com.angorasix.contributors.domain.contributor.ContributorRepository
import com.angorasix.contributors.domain.contributor.ProviderUser
import org.springframework.data.repository.findByIdOrNull

/**
 *
 *
 * @author rozagerardo
 */
class ContributorService(private val repository: ContributorRepository) {

    fun findSingleContributor(providerUser: ProviderUser): Contributor? =
        repository.findDistinctByProviderUsers(providerUser)

    fun findSingleContributor(contributorId: String): Contributor? =
        repository.findByIdOrNull(contributorId)

    fun persistNewLoginContributor(
        providerUser: ProviderUser,
        contributor: Contributor,
    ): Contributor? {
        val existingProviderContributor = repository.findDistinctByProviderUsers(providerUser)
        if (existingProviderContributor == null) {
            val existingContributor = contributor.email?.let { repository.findByEmail(it) }
            val mergedContributor = existingContributor?.let {
                it.mergeProviderUser(providerUser, contributor)
                it
            } ?: contributor
            return repository.save(mergedContributor)
        }
        return existingProviderContributor
    }

    fun checkContributor(
        contributorId: String,
        providerUser: ProviderUser,
    ): Contributor? =
        repository.findDistinctByProviderUsers(providerUser).takeIf { it?.id == contributorId }

    fun updateContributor(
        contributorToUpdate: Contributor,
        updateData: Contributor,
    ): Contributor? = contributorToUpdate.updateWithData(updateData).let { repository.save(it) }

    private fun Contributor.updateWithData(other: Contributor): Contributor {
        other.firstName?.let { this.firstName = it }
        other.lastName?.let { this.lastName = it }
        other.profileMedia?.let { this.profileMedia = it }
        other.headMedia?.let { this.headMedia = it }
        return this
    }

    fun Contributor.mergeProviderUser(
        otherProviderUser: ProviderUser,
        otherContributor: Contributor,
    ) {
        providerUsers.add(otherProviderUser)
        firstName = this.firstName ?: otherContributor.firstName
        lastName = this.lastName ?: otherContributor.lastName
        profileMedia = this.profileMedia ?: otherContributor.profileMedia
        headMedia = this.headMedia ?: otherContributor.headMedia
    }
}
