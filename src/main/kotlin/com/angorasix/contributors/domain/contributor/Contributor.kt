package com.angorasix.contributors.domain.contributor

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
data class Contributor @PersistenceCreator private constructor(
    @field:Id val id: String?,
    val providerUsers: Set<ProviderUser>,
    var email: String?,
    var firstName: String?,
    var lastName: String?,
    val profileMedia: ContributorMedia?,
    val headMedia: ContributorMedia?,
) {

    /**
     * The final constructor that sets all initial fields.
     *
     * @param name - the name of the Project, which will be used to generate the id
     * @param creatorId - a reference to the `Contributor` that created the `Project`
     * @param zone - the `ZoneId` used to indicate the createdAt timestamp
     * @param attributes - a set of initial attributes
     */
    constructor(
        providerUser: ProviderUser,
        email: String?,
        firstName: String? = null,
        lastName: String? = null,
        profileMedia: ContributorMedia? = null,
        headMedia: ContributorMedia? = null,
    ) : this(
        null,
        setOf(providerUser),
        email,
        firstName,
        lastName,
        profileMedia,
        headMedia,
    )
}