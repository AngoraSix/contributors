package com.angorasix.contributors.integration

import com.angorasix.commons.presentation.dto.A6MediaDto
import com.angorasix.contributors.ContributorsApplication
import com.angorasix.contributors.domain.contributor.Contributor
import com.angorasix.contributors.domain.contributor.ProviderUser
import com.angorasix.contributors.integration.utils.IntegrationProperties
import com.angorasix.contributors.integration.utils.initializeMongodb
import com.angorasix.contributors.presentation.dto.ContributorDto
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.hateoas.MediaTypes
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import java.net.URL

@Disabled("Current Spring cloud release train is not compatible with required Boot 3.1")
@SpringBootTest(
    classes = [ContributorsApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@TestPropertySource(locations = ["classpath:integration-application.properties"])
@EnableConfigurationProperties(IntegrationProperties::class)
class ContributorIntegrationTest(
    @Autowired val mongoTemplate: MongoTemplate,
    @Autowired val mapper: ObjectMapper,
    @Autowired val properties: IntegrationProperties,
    @Autowired val webTestClient: WebTestClient,
) {

    @BeforeAll
    fun setUp() = runBlocking {
        initializeMongodb(
            properties.mongodb.baseJsonFile,
            mongoTemplate,
            mapper,
        )
    }

    @Test
    fun `given base data - when retrieve Presentation by id - then existing is retrieved`() {
        val initElementQuery = Query()
        initElementQuery.addCriteria(
            Criteria.where("referenceName")
                .`is`("Project Presentation aimed to devs"),
        )
        val elementId =
            mongoTemplate.findOne(initElementQuery, Contributor::class.java)?.id

        webTestClient.get()
            .uri("/projects-presentation/{projectPresentationId}", elementId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk.expectBody()
            .jsonPath("$.id")
            .exists()
            .jsonPath("$.projectId")
            .isEqualTo("123withSingleSection")
            .jsonPath("$.referenceName")
            .isEqualTo("Project Presentation aimed to devs")
            .jsonPath("$..sections..description")
            .value(hasItem("This is our objective"))
            .jsonPath("$..sections..title")
            .value(hasItem("Join a great project!"))
            .jsonPath("$.sections[0].media.size()")
            .isEqualTo(3)
            .jsonPath("sections[0].mainMedia.resourceId")
            .exists()
            .jsonPath("sections[0].mainMedia.thumbnailUrl")
            .exists()
            .jsonPath("sections[0].mainMedia.url")
            .exists()
            .jsonPath("sections[0].mainMedia.mediaType")
            .exists()
    }

    @Test
    fun `given base data - when get non-existing Presentation - then 404 response`() {
        webTestClient.get()
            .uri("/projects-presentation/non-existing-id")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `when update Contributor data - then supported fields are updated`() {
        val contributorBody = generateContributorDto()
        webTestClient.put()
            .uri("/contributors/1")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaTypes.HAL_FORMS_JSON)
            .body(
                contributorBody,
                ContributorDto::class.java,
            )
            .exchange()
            .expectStatus().isOk.expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.projectId").isEqualTo("567")
            .jsonPath("$.referenceName").isEqualTo("newReferenceName")
            .jsonPath("$..sections.size()").isEqualTo(1)
            .jsonPath("$.sections[0].title").isEqualTo("introduction")
            .jsonPath("$.sections[0].description").isEqualTo("this is a mocked project")
            .jsonPath("$.sections[0].media.size()").isEqualTo(1)
            .jsonPath("$.sections[0].mainMedia.mediaType").isEqualTo("video.youtube")
            .jsonPath("$.sections[0].mainMedia.url")
            .isEqualTo("https://www.youtube.com/watch?v=tHisis4R3soURCeId")
            .jsonPath("$.sections[0].mainMedia.thumbnailUrl").isEqualTo("http://a.video.jpg")
            .jsonPath("$.sections[0].mainMedia.resourceId").isEqualTo("tHisis4R3soURCeId")
    }

    private fun generateContributorDto(): ContributorDto = ContributorDto(
        setOf(ProviderUser(URL("http://issuer.com"), "subject123")),
        "contributor@mail.com",
        "contributorFirstName",
        "contributorLastName",
        A6MediaDto(
            "image",
            "http://image-resource.com/123",
            "http://image-resource.com/123-thumbnail",
            "123",
        ),
        null,
        "cid1",
    )
}
