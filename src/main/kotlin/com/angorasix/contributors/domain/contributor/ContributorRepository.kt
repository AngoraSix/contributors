package com.angorasix.contributors.domain.contributor

import com.angorasix.contributors.infrastructure.persistence.repository.ContributorFilterRepository
import org.springframework.data.repository.CrudRepository

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
interface ContributorRepository :
    CrudRepository<Contributor, String>,
    ContributorFilterRepository {
    fun findByEmail(email: String): Contributor?

    fun findDistinctByProviderUsers(providerUser: ProviderUser): Contributor?
}
