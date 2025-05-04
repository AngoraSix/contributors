package com.angorasix.contributors.infrastructure.persistence.repository

import com.angorasix.contributors.domain.contributor.Contributor
import com.angorasix.contributors.infrastructure.queryfilters.ListContributorsFilter
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ContributorFilterRepositoryImpl(
    private val mongoOps: MongoOperations,
) : ContributorFilterRepository {
    override fun findUsingFilter(filter: ListContributorsFilter): List<Contributor> =
        mongoOps.find(filter.toQuery(), Contributor::class.java)
}

private fun ListContributorsFilter.toQuery(): Query {
    val query = Query()

    if (!id.isNullOrEmpty()) {
        query.addCriteria(where("_id").`in`(id as Collection<Any>))
    } else {
        query.addCriteria(where("_id").`in`(emptyList<String>()))
    }
    return query
}
