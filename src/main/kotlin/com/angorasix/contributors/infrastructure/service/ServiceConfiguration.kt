package com.angorasix.contributors.infrastructure.service

import com.angorasix.contributors.application.ContributorService
import com.angorasix.contributors.domain.contributor.ContributorRepository
import com.angorasix.contributors.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.contributors.presentation.handler.ContributorHandler
import com.angorasix.contributors.presentation.router.ContributorRouter
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServiceConfiguration {
    @Bean
    fun contributorService(repository: ContributorRepository) = ContributorService(repository)

    @Bean
    fun contributorHandler(
        service: ContributorService,
        objectMapper: ObjectMapper,
    ) = ContributorHandler(service, objectMapper)

    @Bean
    fun contributorRouter(
        handler: ContributorHandler,
        apiConfigs: ApiConfigs,
    ) = ContributorRouter(handler, apiConfigs).projectRouterFunction()
}
