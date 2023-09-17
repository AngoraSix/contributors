package com.angorasix.contributors.infrastructure.security

import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

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
            .requestMatchers(HttpMethod.GET, "/contributors/*").permitAll()
            .anyRequest().authenticated()
    } // OAuth2 Login handles the redirect to the OAuth 2.0 Login endpoint
        // from the authorization server filter chain
        .oauth2Login { customizer -> customizer.successHandler(successHandler) }
        .oauth2ResourceServer { oauth2 -> oauth2.jwt(Customizer.withDefaults()) }

    return http.build()
}

fun resourceSecurityFilterChain(
    http: HttpSecurity,
): SecurityFilterChain {
    http.authorizeHttpRequests { authorize ->
        authorize
            .requestMatchers(HttpMethod.GET, "/contributors/*").permitAll()
            .anyRequest().authenticated()
    }.oauth2ResourceServer { oauth2 -> oauth2.jwt(Customizer.withDefaults()) }

    return http.build()
}

fun definePasswordEncoder(): PasswordEncoder =
    PasswordEncoderFactories.createDelegatingPasswordEncoder()
