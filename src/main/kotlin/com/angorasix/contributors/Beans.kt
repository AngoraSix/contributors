package com.angorasix.contributors

import com.angorasix.contributors.application.ContributorService
import com.angorasix.contributors.infrastructure.security.ContributorRepositoryOAuth2UserHandler
import com.angorasix.contributors.infrastructure.security.ContributorRepositoryOidcUserHandler
import com.angorasix.contributors.infrastructure.security.FederatedIdentityAuthenticationSuccessHandler
import com.angorasix.contributors.infrastructure.security.authorizationServerSecurityFilterChain
import com.angorasix.contributors.infrastructure.security.defaultSecurityFilterChain
import com.angorasix.contributors.presentation.handler.ContributorHandler
import com.angorasix.contributors.presentation.router.ContributorRouter
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans

val beans = beans {
    bean{
        authorizationServerSecurityFilterChain(ref())
    }
    bean{
        val authenticationSuccessHandler = FederatedIdentityAuthenticationSuccessHandler()
        authenticationSuccessHandler.setOAuth2UserHandler(ContributorRepositoryOAuth2UserHandler(ref()))
        authenticationSuccessHandler.setOidcUserHandler(ContributorRepositoryOidcUserHandler(ref()))
        defaultSecurityFilterChain(ref(), authenticationSuccessHandler)
    }
    bean<ContributorService>()
    bean<ContributorHandler>()
    bean {
        ContributorRouter(ref(), ref(), ref()).projectRouterFunction()
    }
}

class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(context: GenericApplicationContext) = beans.initialize(context)
}
