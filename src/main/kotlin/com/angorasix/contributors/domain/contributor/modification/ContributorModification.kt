package com.angorasix.contributors.domain.contributor.modification

import com.angorasix.commons.domain.A6Contributor
import com.angorasix.commons.domain.modification.DomainObjectModification
import com.angorasix.contributors.domain.contributor.Contributor
import com.angorasix.contributors.domain.contributor.ContributorMedia

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
abstract class ContributorModification<U>(
    modifyValue: U,
) : DomainObjectModification<Contributor, U>(modifyValue)

class UpdateHeadMedia(
    fieldValue: ContributorMedia,
) : ContributorModification<ContributorMedia>(fieldValue) {
    override fun modify(
        requestingContributor: A6Contributor,
        domainObject: Contributor,
    ): Contributor {
        require(
            (requestingContributor.contributorId == domainObject.id),
        ) { "Can't modify headMedia field for contributor" }
        domainObject.headMedia = modifyValue
        return domainObject
    }
}

class UpdateProfileMedia(
    fieldValue: ContributorMedia,
) : ContributorModification<ContributorMedia>(fieldValue) {
    override fun modify(
        requestingContributor: A6Contributor,
        domainObject: Contributor,
    ): Contributor {
        require(
            (requestingContributor.contributorId == domainObject.id),
        ) { "Can't modify profileMedia field for contributor" }
        domainObject.profileMedia = modifyValue
        return domainObject
    }
}
