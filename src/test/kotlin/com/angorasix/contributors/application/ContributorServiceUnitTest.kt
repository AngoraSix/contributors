package com.angorasix.contributors.application

import com.angorasix.contributors.domain.contributor.Contributor
import com.angorasix.contributors.domain.contributor.ContributorMedia
import com.angorasix.contributors.domain.contributor.ContributorRepository
import com.angorasix.contributors.domain.contributor.ProviderUser
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.assertj.core.api.InstanceOfAssertFactories
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import java.net.URI

@ExtendWith(MockKExtension::class)
class ContributorServiceUnitTest {
    private lateinit var service: ContributorService

    @MockK
    private lateinit var repository: ContributorRepository

    @BeforeEach
    fun init() {
        service = ContributorService(repository)
    }

    @Test
    @Throws(Exception::class)
    fun `when request get single contributor by provider user - then receive contributor`() =
        runTest {
            val providerUser = ProviderUser(URI("http://issuer.com").toURL(), "subject123")
            val mockedContributor = generateContributor(providerUser)
            every { repository.findDistinctByProviderUsers(providerUser) } returns mockedContributor

            val outputContributor = service.findSingleContributor(providerUser)

            assertThat(outputContributor).isSameAs(mockedContributor)

            verify { repository.findDistinctByProviderUsers(providerUser) }
        }

    @Test
    @Throws(Exception::class)
    fun `when request get single contributor by id - then receive contributor`() =
        runTest {
            val contributorId = "c123"
            val mockedContributor =
                generateContributor(ProviderUser(URI("http://issuer.com").toURL(), "subject123"))
            every { repository.findByIdOrNull(contributorId) } returns mockedContributor

            val outputContributor = service.findSingleContributor(contributorId)

            assertThat(outputContributor).isSameAs(mockedContributor)

            verify { repository.findByIdOrNull(contributorId) }
        }

    @Test
    @Throws(Exception::class)
    fun `when persist existing login contributor - then contributor not saved`() =
        runTest {
            val providerUser = ProviderUser(URI("http://issuer.com").toURL(), "subject123")
            val mockedContributor = generateContributor(providerUser)
            every { repository.findDistinctByProviderUsers(providerUser) } returns mockedContributor

            val outputContributor =
                service.persistNewLoginContributor(providerUser, mockedContributor)

            assertThat(outputContributor).isSameAs(mockedContributor)

            verify { repository.findDistinctByProviderUsers(providerUser) }
        }

    @Test
    @Throws(Exception::class)
    fun `when persist existing new contributor - then saved to repo called`() =
        runTest {
            val providerUser = ProviderUser(URI("http://issuer.com").toURL(), "subject123")
            val mockedContributor = generateContributor(providerUser)
            every { repository.findDistinctByProviderUsers(providerUser) } returns null
            every { repository.findByEmail("contributor@mail.com") } returns null
            val savedContributor = generateContributor(providerUser)
            every { repository.save(mockedContributor) } returns savedContributor

            val outputContributor =
                service.persistNewLoginContributor(providerUser, mockedContributor)

            assertThat(outputContributor).isSameAs(savedContributor)

            verify { repository.findDistinctByProviderUsers(providerUser) }
            verify { repository.findByEmail("contributor@mail.com") }
            verify { repository.save(mockedContributor) }
        }

    @Test
    @Throws(Exception::class)
    fun `when persist existing contributor with new provider user - then saved to repo called`() =
        runTest {
            val providerUser = ProviderUser(URI("http://other.com").toURL(), "subject456")
            val mockedContributor = generateContributor(providerUser)
            every { repository.findDistinctByProviderUsers(providerUser) } returns null
            val existingProviderUser = ProviderUser(URI("http://issuer.com").toURL(), "subject123")
            val existingContributor =
                Contributor(
                    existingProviderUser,
                    "contributor@mail.com",
                    "existingFirstName",
                    "existingLastName",
                    null,
                    ContributorMedia(
                        "image",
                        "http://image-resource.com/456",
                        "http://image-resource.com/456-thumbnail",
                        "456",
                    ),
                )
            every { repository.findByEmail("contributor@mail.com") } returns existingContributor
            val updatedContributor = slot<Contributor>()
            val savedContributor = generateContributor(providerUser)
            every { repository.save(capture(updatedContributor)) } returns savedContributor

            val outputContributor =
                service.persistNewLoginContributor(providerUser, mockedContributor)
            val capturedUpdatedContributor = updatedContributor.captured

            assertThat(outputContributor).isSameAs(savedContributor)
            assertThat(capturedUpdatedContributor.firstName).isEqualTo(existingContributor.firstName)
            assertThat(capturedUpdatedContributor.lastName).isEqualTo(existingContributor.lastName)
            assertThat(capturedUpdatedContributor.profileMedia).isEqualTo(existingContributor.profileMedia)
            assertThat(capturedUpdatedContributor.headMedia).isEqualTo(existingContributor.headMedia)
            assertThat(capturedUpdatedContributor.providerUsers.toList())
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .contains(providerUser, existingProviderUser)

            verify { repository.findDistinctByProviderUsers(providerUser) }
            verify { repository.findByEmail("contributor@mail.com") }
            verify { repository.save(capturedUpdatedContributor) }
        }

    @Test
    @Throws(Exception::class)
    fun `when check contributor with correct id - then contributor returned`() =
        runTest {
            val contributorId = "c123"
            val providerUser = ProviderUser(URI("http://issuer.com").toURL(), "subject123")
            val mockedContributor = mockkClass(Contributor::class)
            every { repository.findDistinctByProviderUsers(providerUser) } returns mockedContributor
            every { mockedContributor.id } returns contributorId

            val outputContributor =
                service.checkContributor(contributorId, providerUser)

            assertThat(outputContributor).isSameAs(mockedContributor)

            verify { repository.findDistinctByProviderUsers(providerUser) }
            verify { mockedContributor.id }
        }

    @Test
    @Throws(Exception::class)
    fun `when check contributor with mismatching id - then null returned`() =
        runTest {
            val contributorId = "c123"
            val otherContributorId = "other456"
            val providerUser = ProviderUser(URI("http://issuer.com").toURL(), "subject123")
            val mockedContributor = mockkClass(Contributor::class)
            every { repository.findDistinctByProviderUsers(providerUser) } returns mockedContributor
            every { mockedContributor.id } returns otherContributorId

            val outputContributor =
                service.checkContributor(contributorId, providerUser)

            assertThat(outputContributor).isNull()

            verify { repository.findDistinctByProviderUsers(providerUser) }
            verify { mockedContributor.id }
        }

    private fun generateContributor(providerUser: ProviderUser): Contributor =
        Contributor(
            providerUser,
            "contributor@mail.com",
            "contributorFirstName",
            "contributorLastName",
            ContributorMedia(
                "image",
                "http://image-resource.com/123",
                "http://image-resource.com/123-thumbnail",
                "123",
            ),
            null,
        )
}
