package com.angorasix.contributors

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.hateoas.config.EnableHypermediaSupport
import org.springframework.hateoas.support.WebStack

@SpringBootApplication
@EnableHypermediaSupport(
    type = [EnableHypermediaSupport.HypermediaType.HAL_FORMS],
    stacks = [WebStack.WEBMVC],
)
@ConfigurationPropertiesScan("com.angorasix.contributors.infrastructure.config.configurationproperty.api")
class ContributorsApplication

fun main(args: Array<String>) {
    runApplication<ContributorsApplication>(*args)
}
