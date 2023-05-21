package com.angorasix.contributors.infrastructure.security

import com.angorasix.contributors.application.ContributorService
import com.angorasix.contributors.infrastructure.config.security.oauth.A6WellKnownClaims
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.http.MediaType
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.OAuth2Token
import org.springframework.security.oauth2.core.oidc.StandardClaimNames
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher


/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
fun authorizationServerSecurityFilterChain(
    http: HttpSecurity,
    generator: OAuth2TokenGenerator<OAuth2Token>,
): SecurityFilterChain {
    OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)
    http.getConfigurer(OAuth2AuthorizationServerConfigurer::class.java)
        .oidc(Customizer.withDefaults()) // Enable OpenID Connect 1.0
        .tokenGenerator(generator)
    http // Redirect to the OAuth 2.0 Login endpoint when not authenticated
        // from the authorization endpoint
        .exceptionHandling { exceptions ->
            exceptions
                .defaultAuthenticationEntryPointFor(
                    LoginUrlAuthenticationEntryPoint("/oauth2/authorization/google"),
                    MediaTypeRequestMatcher(MediaType.TEXT_HTML),
                )
        } // Accept access tokens for User Info and/or Client Registration
        .oauth2ResourceServer { oauth2 -> oauth2.jwt(Customizer.withDefaults()) }
    return http.build()
}

fun tokenCustomizer(
    contibutorService: ContributorService,
): OAuth2TokenCustomizer<JwtEncodingContext> {
    return OAuth2TokenCustomizer { context: JwtEncodingContext ->
        if (OidcParameterNames.ID_TOKEN.equals(context.tokenType.value)) {
            val principal = context.getPrincipal<Authentication>()
            val providerUser = extractProviderUser(principal)
            val contributor = contibutorService.findSingleContributor(providerUser)
            context.claims.claims { claims: MutableMap<String?, Any?> ->
                claims.putAll(
                    arrayOf(
                        StandardClaimNames.GIVEN_NAME to contributor?.firstName,
                        StandardClaimNames.FAMILY_NAME to contributor?.lastName,
                        StandardClaimNames.EMAIL to contributor?.email,
                        A6WellKnownClaims.PROFILE_IMAGE to contributor?.profileMedia?.url,
                        A6WellKnownClaims.PROFILE_IMAGE_THUMBNAIL to contributor?.profileMedia?.thumbnailUrl,
                        A6WellKnownClaims.HEAD_IMAGE to contributor?.headMedia?.url,
                        A6WellKnownClaims.HEAD_IMAGE_THUMBNAIL to contributor?.headMedia?.thumbnailUrl,
                    ),
                )
            }
        }
    }
}

fun tokenGenerator(
    tokenCustomizer: OAuth2TokenCustomizer<JwtEncodingContext>,
    jwkSource: JWKSource<SecurityContext>
): OAuth2TokenGenerator<OAuth2Token> {
    var jwtGenerator = JwtGenerator(NimbusJwtEncoder(jwkSource))
    jwtGenerator.setJwtCustomizer(tokenCustomizer)
    val accessTokenGenerator = OAuth2AccessTokenGenerator()
    val refreshTokenGenerator = OAuth2RefreshTokenGenerator()
    return DelegatingOAuth2TokenGenerator(
        jwtGenerator, accessTokenGenerator, refreshTokenGenerator,
    )
}