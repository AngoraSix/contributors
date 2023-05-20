package com.angorasix.contributors.application

import com.angorasix.contributors.domain.contributor.Contributor
import com.angorasix.contributors.domain.contributor.ContributorRepository
import com.angorasix.contributors.domain.contributor.ProviderUser
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.findByIdOrNull

/**
 *
 *
 * @author rozagerardo
 */
class ContributorService(private val repository: ContributorRepository) {

    fun findSingleContributor(providerUser: ProviderUser): Contributor? =
        repository.findDistinctByProviderUsers(providerUser)
}
