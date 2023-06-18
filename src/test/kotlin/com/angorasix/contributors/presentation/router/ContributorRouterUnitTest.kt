package com.angorasix.contributors.presentation.router

import com.angorasix.contributors.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.contributors.infrastructure.config.configurationproperty.api.BasePathConfigs
import com.angorasix.contributors.infrastructure.config.configurationproperty.api.Route
import com.angorasix.contributors.infrastructure.config.configurationproperty.api.RoutesConfigs
import com.angorasix.contributors.presentation.handler.ContributorHandler
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpMethod
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@ExtendWith(MockKExtension::class)
class ContributorRouterUnitTest {

    private lateinit var router: ContributorRouter

    @MockK
    private lateinit var apiConfigs: ApiConfigs

    @MockK
    private lateinit var handler: ContributorHandler

    private var routeConfigs: RoutesConfigs = RoutesConfigs(
        "",
        "/{id}",
        Route("mocked-get-single", listOf("mocked-base1"), HttpMethod.GET, "/{id}"),
        Route("mocked-modify", listOf("mocked-base1"), HttpMethod.PATCH, "/{id}"),
    )
    private var basePathsConfigs: BasePathConfigs = BasePathConfigs("/contributors")

    @BeforeEach
    fun init() {
        every { apiConfigs.routes } returns routeConfigs
        every { apiConfigs.basePaths } returns basePathsConfigs
        router = ContributorRouter(handler, apiConfigs)
    }

    @Test
    @Throws(Exception::class)
    fun `Given Project router - When expected APIs requested - Then router routes correctly`() =
        runTest {
            val outputRouter = router.projectRouterFunction()
            val getSingleContributorRequest = ServerRequest.create(MockHttpServletRequest("GET", "/contributors/123"), emptyList())
            val getPatchProjectRequest = ServerRequest.create(MockHttpServletRequest("PATCH", "/contributors/123"), emptyList())
            val invalidRequest = ServerRequest.create(MockHttpServletRequest("GET", "/other/anything"), emptyList())
            val mockedResponse = ServerResponse.ok().build()
            every { handler.getContributor(getSingleContributorRequest) } returns mockedResponse
            every { handler.patchContributor(getPatchProjectRequest) } returns mockedResponse

            // if routes don't match, they will throw an exception as with the invalid Route no need to assert anything
            outputRouter.route(getSingleContributorRequest).get().handle(getSingleContributorRequest)
            outputRouter.route(getPatchProjectRequest).get().handle(getPatchProjectRequest)
            assertThat(outputRouter.route(invalidRequest)).isEmpty
        }
}
