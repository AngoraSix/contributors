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
    val providerUsers: MutableSet<ProviderUser>,
    val email: String?,
    var firstName: String?,
    var lastName: String?,
    var profileMedia: ContributorMedia?,
    var headMedia: ContributorMedia?,
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
        mutableSetOf(providerUser),
        email,
        firstName,
        lastName,
        profileMedia,
        headMedia,
    )

    fun mergeProviderUser(providerUser: ProviderUser, contributor: Contributor) {
        this.providerUsers.add(providerUser)
        this.firstName = this.firstName ?: contributor.firstName
        this.lastName = this.lastName ?: contributor.lastName
        this.profileMedia = this.profileMedia ?: contributor.profileMedia
        this.headMedia = this.headMedia ?: contributor.headMedia
    }
}
