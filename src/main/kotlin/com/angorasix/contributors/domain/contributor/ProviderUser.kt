package com.angorasix.contributors.domain.contributor

import java.net.URL

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
data class ProviderUser(
    val issuer: URL,
    val subject: String,
)
