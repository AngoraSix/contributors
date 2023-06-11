package com.angorasix.contributors.presentation.handler

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.infrastructure.constants.AngoraSixInfrastructure
import com.angorasix.commons.infrastructure.oauth2.constants.A6WellKnownClaims
import com.angorasix.contributors.application.ContributorService
import com.angorasix.contributors.domain.contributor.Contributor
import com.angorasix.contributors.domain.contributor.ContributorMedia
import com.angorasix.contributors.domain.contributor.ProviderUser
import com.angorasix.contributors.presentation.dto.ContributorDto
import com.angorasix.contributors.presentation.dto.ContributorMediaDto
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.servlet.function.EntityResponse
import org.springframework.web.servlet.function.RouterFunctions
import org.springframework.web.servlet.function.ServerRequest.create
import java.net.URL
import java.time.Instant

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class ContributorHandlerUnitTest {

    private lateinit var handler: ContributorHandler

    @MockK
    private lateinit var service: ContributorService

    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun init() {
        handler = ContributorHandler(service)
    }

    @Test
    @Throws(Exception::class)
    fun `When update contributor - Then handler retrieves Updated`() =
        run {
            val mockedContributorDto = generateContributorDto(
                "updated",
                withProfileMedia = false,
                withHeadMedia = true,
            )
            val mockedRequest = mockHttpServletRequest()
            mockedRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            mockedRequest.setContent(objectMapper.writeValueAsBytes(mockedContributorDto))
            val existingContributor = generateContributor()
            val savedContributor = generateContributor()
            val updateSingleContributorRequest =
                create(
                    mockedRequest,
                    listOf(MappingJackson2HttpMessageConverter()),
                )
            every {
                service.checkContributor(
                    "cid-123",
                    ProviderUser(URL("http://localhost:9081"), "sub-123"),
                )
            } returns existingContributor
            every {
                service.updateContributor(
                    existingContributor,
                    ofType(Contributor::class),
                )
            } returns savedContributor

            val outputResponse = handler.updateContributor(updateSingleContributorRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
            val response = @Suppress("UNCHECKED_CAST")
            outputResponse as EntityResponse<ContributorDto>
            val responseBody = response.entity()
            assertThat(responseBody).isNotSameAs(mockedContributorDto)
            assertThat(responseBody.firstName).isEqualTo("contributorFirstName")
            assertThat(responseBody.lastName).isEqualTo("contributorLastName")
            assertThat(responseBody.email).isEqualTo("contributor@mail.com")
            assertThat(responseBody.headMedia?.url).isNull()
            assertThat(responseBody.profileMedia?.url).isEqualTo("http://image-profile.com/123")
            verify {
                service.checkContributor(
                    "cid-123",
                    ProviderUser(URL("http://localhost:9081"), "sub-123"),
                )
            }
            verify {
                service.updateContributor(
                    existingContributor,
                    ofType(Contributor::class),
                )
            }
        }

    @Test
    @Throws(Exception::class)
    fun `When get project - Then handler retrieves resource`() =
        run {
            val mockedRequest = mockHttpServletRequest()
            val existingContributor = generateContributor()
            val getSingleContributorRequest =
                create(
                    mockedRequest,
                    listOf(MappingJackson2HttpMessageConverter()),
                )
            every {
                service.findSingleContributor(
                    "cid-123",
                )
            } returns existingContributor

            val outputResponse = handler.getContributor(getSingleContributorRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
            val response = @Suppress("UNCHECKED_CAST")
            outputResponse as EntityResponse<ContributorDto>
            val responseBody = response.entity()
            assertThat(responseBody.providerUsers.toList()).asList().isEmpty()
            assertThat(responseBody.email).isEqualTo("contributor@mail.com")
            assertThat(responseBody.firstName).isEqualTo("contributorFirstName")
            assertThat(responseBody.lastName).isEqualTo("contributorLastName")
            verify {
                service.findSingleContributor(
                    "cid-123",
                )
            }
        }

    private fun generateContributor(
        prefix: String? = "",
        withProfileMedia: Boolean = true,
        withHeadMedia: Boolean = false,
    ): Contributor = Contributor(
        ProviderUser(URL("http://issuer.com"), "${prefix}subject123"),
        "${prefix}contributor@mail.com",
        "${prefix}contributorFirstName",
        "${prefix}contributorLastName",
        if (withProfileMedia) {
            ContributorMedia(
                "image",
                "http://image-profile.com/123",
                "http://image-profile.com/123-thumbnail",
                "123",
            )
        } else {
            null
        },
        if (withHeadMedia) {
            ContributorMedia(
                "image",
                "http://image-head.com/456",
                "http://image-head.com/456-thumbnail",
                "456",
            )
        } else {
            null
        },
    )

    private fun generateContributorDto(
        prefix: String? = "",
        withProfileMedia: Boolean = true,
        withHeadMedia: Boolean = false,
    ): ContributorDto = ContributorDto(
        setOf(ProviderUser(URL("http://issuer.com"), "${prefix}subject123")),
        "${prefix}contributor@mail.com",
        "${prefix}contributorFirstName",
        "${prefix}contributorLastName",
        if (withProfileMedia) {
            ContributorMediaDto(
                "image",
                "http://image-profile.com/123",
                "http://image-profile.com/123-thumbnail",
                "123",
            )
        } else {
            null
        },
        if (withHeadMedia) {
            ContributorMediaDto(
                "image",
                "http://image-head.com/456",
                "http://image-head.com/456-thumbnail",
                "456",
            )
        } else {
            null
        },
        "${prefix}cid",
    )

    private fun mockHttpServletRequest(): MockHttpServletRequest {
        val mockedRequest = MockHttpServletRequest("PUT", "/contributors/123")
        val mockedSimpleContributor = SimpleContributor("mockedId")
        val pathVariables = mapOf("id" to "cid-123")
        mockedRequest.setAttribute(
            RouterFunctions.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
            pathVariables,
        )
        mockedRequest.setAttribute(
            AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY,
            mockedSimpleContributor,
        )
        val jwt: Jwt =
            Jwt.withTokenValue("tokenValue").expiresAt(Instant.now().plusSeconds(5000))
                .issuer("http://localhost:9081")
                .subject("sub-123")
                .header("alg", "ger")
                .claim(A6WellKnownClaims.CONTRIBUTOR_ID, "contributorIdValue").build()
        mockedRequest.userPrincipal = JwtAuthenticationToken(jwt)
        return mockedRequest
    }
}
