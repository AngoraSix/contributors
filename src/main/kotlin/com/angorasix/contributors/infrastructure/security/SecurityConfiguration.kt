package com.angorasix.contributors.infrastructure.security

import org.springframework.http.MediaType
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher


/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
    OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)
    http.getConfigurer(OAuth2AuthorizationServerConfigurer::class.java)
        .oidc(Customizer.withDefaults()) // Enable OpenID Connect 1.0
    http // Redirect to the OAuth 2.0 Login endpoint when not authenticated
        // from the authorization endpoint
        .exceptionHandling { exceptions ->
            exceptions
                .defaultAuthenticationEntryPointFor(
                    LoginUrlAuthenticationEntryPoint("/oauth2/authorization/google"),
                    MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                )
        } // Accept access tokens for User Info and/or Client Registration
        .oauth2ResourceServer { oauth2 -> oauth2.jwt(Customizer.withDefaults()) }
    return http.build()
}

fun defaultSecurityFilterChain(http: HttpSecurity, successHandler: AuthenticationSuccessHandler): SecurityFilterChain {
    http.authorizeHttpRequests { authorize ->
        authorize
            .anyRequest().authenticated()
    } // OAuth2 Login handles the redirect to the OAuth 2.0 Login endpoint
        // from the authorization server filter chain
        .oauth2Login { customizer -> customizer.successHandler(successHandler) }

    return http.build()
}