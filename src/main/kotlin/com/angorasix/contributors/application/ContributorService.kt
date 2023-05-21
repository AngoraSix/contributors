package com.angorasix.contributors.application

import com.angorasix.contributors.domain.contributor.Contributor
import com.angorasix.contributors.domain.contributor.ContributorRepository
import com.angorasix.contributors.domain.contributor.ProviderUser

/**
 *
 *
 * @author rozagerardo
 */
class ContributorService(private val repository: ContributorRepository) {

    fun findSingleContributor(providerUser: ProviderUser): Contributor? =
        repository.findDistinctByProviderUsers(providerUser)

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
}
