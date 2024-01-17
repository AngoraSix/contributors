package com.angorasix.contributors.domain.contributor

import com.angorasix.commons.domain.A6Media

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ContributorMedia(
    mediaType: String,
    url: String,
    thumbnailUrl: String,
    resourceId: String,
) : A6Media(mediaType, url, thumbnailUrl, resourceId)
