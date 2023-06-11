package com.angorasix.contributors.domain.contributor

import org.springframework.data.repository.CrudRepository

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
interface ContributorRepository : CrudRepository<Contributor, String> {
    fun findByEmail(email: String): Contributor?

    fun findDistinctByProviderUsers(providerUser: ProviderUser): Contributor?
}
