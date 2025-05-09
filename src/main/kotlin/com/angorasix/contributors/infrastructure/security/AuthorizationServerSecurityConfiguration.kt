package com.angorasix.contributors.infrastructure.security

import com.angorasix.commons.infrastructure.oauth2.constants.A6WellKnownClaims
import com.angorasix.contributors.application.ContributorService
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.OAuth2Token
import org.springframework.security.oauth2.core.oidc.StandardClaimNames
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
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
@Configuration
class AuthorizationServerSecurityConfiguration {
    @Bean
    fun authorizationServerSecurityFilterChain(
        http: HttpSecurity,
        generator: OAuth2TokenGenerator<OAuth2Token>,
    ): SecurityFilterChain {
        // 1) Create the OAuth2AuthorizationServerConfigurer
        val authorizationServerConfigurer =
            OAuth2AuthorizationServerConfigurer.authorizationServer()
        http
            // 2) Apply it only to the OAuth2 endpoints
            .securityMatcher(authorizationServerConfigurer.endpointsMatcher)
            // 3) Inside that matcher, configure the OAuth2 endpoints…
            .with(authorizationServerConfigurer) { cfg ->
                cfg
                    .oidc(Customizer.withDefaults()) // enable OIDC
                    .tokenGenerator(generator) // your custom token generator
            }
            // 4) all those endpoints require auth
            .authorizeHttpRequests { auth ->
                auth.anyRequest().authenticated()
            }
            // 5) disable CSRF on the OAuth2 endpoints ???
//            .csrf { csrf ->
//                csrf.ignoringRequestMatchers(authorizationServerConfigurer.endpointsMatcher)
//            }
            // 6) when someone unauthenticated hits /oauth2/authorize, send them to your login page
            .exceptionHandling { exceptions ->
                exceptions.defaultAuthenticationEntryPointFor(
                    LoginUrlAuthenticationEntryPoint("/oauth2/authorization/google"),
                    MediaTypeRequestMatcher(MediaType.TEXT_HTML),
                )
            }
            // 7) and accept JWTs at your UserInfo / revocation / introspection endpoints
            .oauth2ResourceServer { oauth2 -> oauth2.jwt(Customizer.withDefaults()) }
        return http.build()
    }

    @Bean
    fun tokenCustomizer(contibutorService: ContributorService): OAuth2TokenCustomizer<JwtEncodingContext> =
        OAuth2TokenCustomizer { context: JwtEncodingContext ->
            val principal = context.getPrincipal<Authentication>()
            val providerUser = extractProviderUser(principal)
            val contributor = contibutorService.findSingleContributor(providerUser)
            if (OidcParameterNames.ID_TOKEN == context.tokenType.value) {
                context.claims.claims { claims: MutableMap<String?, Any?> ->
                    claims.putAll(
                        arrayOf(
                            StandardClaimNames.GIVEN_NAME to contributor?.firstName,
                            StandardClaimNames.FAMILY_NAME to contributor?.lastName,
                            StandardClaimNames.EMAIL to contributor?.email,
                            A6WellKnownClaims.CONTRIBUTOR_ID to contributor?.id,
                            A6WellKnownClaims.PROFILE_IMAGE to contributor?.profileMedia?.url,
                            A6WellKnownClaims.PROFILE_IMAGE_THUMBNAIL to contributor?.profileMedia?.thumbnailUrl,
                            A6WellKnownClaims.HEAD_IMAGE to contributor?.headMedia?.url,
                            A6WellKnownClaims.HEAD_IMAGE_THUMBNAIL to contributor?.headMedia?.thumbnailUrl,
                        ),
                    )
                }
            }
            if (OAuth2TokenType.ACCESS_TOKEN == context.tokenType) {
                context.claims.claims { claims ->
                    claims.putAll(
                        arrayOf(
                            A6WellKnownClaims.CONTRIBUTOR_ID to contributor?.id,
                            StandardClaimNames.NAME to (
                                contributor?.firstName
                                    ?: contributor?.lastName
                            ),
                            StandardClaimNames.PICTURE to (contributor?.profileMedia?.url),
                        ),
                    )
                }
            }
        }

    @Bean
    fun tokenGenerator(
        tokenCustomizer: OAuth2TokenCustomizer<JwtEncodingContext>,
        jwkSource: JWKSource<SecurityContext>,
    ): OAuth2TokenGenerator<OAuth2Token> {
        var jwtGenerator = JwtGenerator(NimbusJwtEncoder(jwkSource))
        jwtGenerator.setJwtCustomizer(tokenCustomizer)
        val accessTokenGenerator = OAuth2AccessTokenGenerator()
        val refreshTokenGenerator = OAuth2RefreshTokenGenerator()
        return DelegatingOAuth2TokenGenerator(
            jwtGenerator,
            accessTokenGenerator,
            refreshTokenGenerator,
        )
    }
}
