package com.angorasix.contributors.infrastructure.queryfilters

import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

/**
 * <p> Classes containing different Request Query Filters.
 * </p>
 *
 * @author rozagerardo
 */
data class ListContributorsFilter(
    val id: Collection<String>? = null,
) {
    fun toMultiValueMap(): MultiValueMap<String, String> {
        val multiMap: MultiValueMap<String, String> = LinkedMultiValueMap()
        multiMap.add("id", id?.joinToString(","))
        return multiMap
    }

    companion object {
        fun fromMultiValueMap(multiMap: MultiValueMap<String, String>): ListContributorsFilter =
            ListContributorsFilter(
                multiMap.getFirst("id")?.split(","),
            )
    }
}
