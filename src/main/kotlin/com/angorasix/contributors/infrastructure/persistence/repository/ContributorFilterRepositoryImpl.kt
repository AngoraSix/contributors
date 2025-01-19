package com.angorasix.contributors.infrastructure.persistence.repository

import com.angorasix.contributors.domain.contributor.Contributor
import com.angorasix.contributors.infrastructure.queryfilters.ListContributorsFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ContributorFilterRepositoryImpl(private val mongoOps: MongoOperations) :
    ContributorFilterRepository {

    override fun findUsingFilter(
        filter: ListContributorsFilter,
    ): List<Contributor> {
        return mongoOps.find(filter.toQuery(), Contributor::class.java)
    }
}

private fun ListContributorsFilter.toQuery(): Query {
    val query = Query()

    query.addCriteria(where("id").`in`(id))

    return query
}
