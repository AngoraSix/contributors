package com.angorasix.contributors.infrastructure.security

import com.angorasix.contributors.application.ContributorService
import com.angorasix.contributors.domain.contributor.Contributor
import com.angorasix.contributors.domain.contributor.ContributorMedia
import com.angorasix.contributors.domain.contributor.ProviderUser
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import java.net.URL
import java.security.Principal
import java.util.function.Consumer

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class FederatedIdentityAuthenticationSuccessHandler : AuthenticationSuccessHandler {
    private val delegate: AuthenticationSuccessHandler =
        SavedRequestAwareAuthenticationSuccessHandler()
    private var oauth2UserHandler: Consumer<OAuth2User> = Consumer<OAuth2User> { user -> }
    private var oidcUserHandler: Consumer<OidcUser> =
        Consumer<OidcUser> { user ->
            oauth2UserHandler.accept(
                user,
            )
        }

    override fun onAuthenticationSuccess(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authentication: Authentication,
    ) {
        if (authentication is OAuth2AuthenticationToken) {
            if (authentication.principal is OidcUser) {
                oidcUserHandler.accept(authentication.principal as OidcUser)
            } else if (authentication.principal is OAuth2User) {
                oauth2UserHandler.accept(authentication.principal as OAuth2User)
            }
        }
        delegate.onAuthenticationSuccess(request, response, authentication)
    }

    fun setOAuth2UserHandler(oauth2UserHandler: Consumer<OAuth2User>) {
        this.oauth2UserHandler = oauth2UserHandler
    }

    fun setOidcUserHandler(oidcUserHandler: Consumer<OidcUser>) {
        this.oidcUserHandler = oidcUserHandler
    }
}

class ContributorRepositoryOAuth2UserHandler(
    val contributorService: ContributorService,
) : Consumer<OAuth2User> {
    override fun accept(user: OAuth2User) {
        val providerUser =
            generateOAuth2ProviderUser(user)
        val profileMedia =
            (user.attributes["picture"] as String?)?.let { ContributorMedia("image", it, it, it) }
                ?: null
        val contributor =
            Contributor(
                providerUser,
                user.attributes["email"] as String?,
                (user.attributes["given_name"] ?: user.attributes["name"] ?: user.name) as String?,
                user.attributes["family_name"] as String?,
                profileMedia,
            )
        persistContributor(
            contributorService,
            providerUser,
            contributor,
        )
    }
}

class ContributorRepositoryOidcUserHandler(
    val contributorService: ContributorService,
) : Consumer<OidcUser> {
    override fun accept(user: OidcUser) {
        val providerUser =
            generateOidcProviderUser(user)
        val profileMedia = user.picture?.let { ContributorMedia("image", it, it, it) } ?: null
        val contributor =
            Contributor(
                providerUser,
                user.email,
                user.givenName ?: user.nickName ?: user.preferredUsername ?: user.fullName ?: user.name,
                user.familyName,
                profileMedia,
            )
        persistContributor(
            contributorService,
            providerUser,
            contributor,
        )
    }
}

private fun generateOidcProviderUser(user: OidcUser): ProviderUser = generateProviderUser(user.issuer, user.subject)

private fun generateOAuth2ProviderUser(user: OAuth2User): ProviderUser =
    generateProviderUser(user.attributes["iss"] as URL?, user.attributes["sub"] as String?)

private fun generateJwtProviderUser(user: Jwt): ProviderUser = generateProviderUser(user.issuer, user.subject)

private fun generateProviderUser(
    issuer: URL?,
    subject: String?,
): ProviderUser {
    if (issuer == null || subject == null) {
        throw IllegalArgumentException("Login data doesn't include required 'iss' and 'sub' parameters")
    }
    return ProviderUser(issuer, subject)
}

private fun persistContributor(
    contributorService: ContributorService,
    providerUser: ProviderUser,
    contributor: Contributor,
) {
    // Capture user in a local data store on first authentication
    contributorService.persistNewLoginContributor(providerUser, contributor)
}

fun extractProviderUser(principal: Principal): ProviderUser =
    when (principal) {
        is OidcUser -> {
            generateOidcProviderUser(principal)
        }

        is OAuth2User -> {
            generateOAuth2ProviderUser(principal)
        }

        is OAuth2AuthenticationToken -> {
            generateOAuth2ProviderUser(principal.principal)
        }

        is JwtAuthenticationToken -> {
            generateJwtProviderUser(principal.token)
        }

        else -> throw IllegalArgumentException("Authentication is not based on OAuth Login")
    }
