package com.angorasix.contributors.infrastructure.security

import org.springframework.http.MediaType
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator
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
fun defaultSecurityFilterChain(
    http: HttpSecurity,
    successHandler: AuthenticationSuccessHandler,
): SecurityFilterChain {
    http.authorizeHttpRequests { authorize ->
        authorize
            .anyRequest().authenticated()
    } // OAuth2 Login handles the redirect to the OAuth 2.0 Login endpoint
        // from the authorization server filter chain
        .oauth2Login { customizer -> customizer.successHandler(successHandler) }

    return http.build()
}

fun definePasswordEncoder(): PasswordEncoder =
    PasswordEncoderFactories.createDelegatingPasswordEncoder()