package com.angorasix.contributors.infrastructure.persistence.repository

import com.angorasix.contributors.domain.contributor.Contributor
import com.angorasix.contributors.infrastructure.queryfilters.ListContributorsFilter

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
interface ContributorFilterRepository {

    fun findUsingFilter(
        filter: ListContributorsFilter,
    ): List<Contributor>
}
