package com.mobimeo.challenge

import com.mobimeo.challenge.model.Asset
import com.mobimeo.challenge.model.Customer
import com.mobimeo.challenge.model.CustomerWithAssets
import com.mobimeo.challenge.repository.StarWarsRepository
import com.mobimeo.challenge.service.external.ExternalStarWarsInfo
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.core.publisher.Mono
import java.util.*


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Execution(ExecutionMode.SAME_THREAD)
class IntegrationTests {

    @LocalServerPort
    private var port = 0

    private final val testCustomer = Customer(
        id = UUID.randomUUID(),
        externalId = 1L,
        name = "Luke Skywalker",
    )

    private final val testAssets = listOf(
        Asset(
            id = UUID.randomUUID(),
            customerId = testCustomer.id,
            name = "Snowspeeder",
            type = Asset.Type.VEHICLE,
            externalId = 14,
            value = 0,
        ),
        Asset(
            id = UUID.randomUUID(),
            customerId = testCustomer.id,
            name = "Imperial Speeder Bike",
            type = Asset.Type.VEHICLE,
            externalId = 30,
            value = 8000,
        ),
        Asset(
            id = UUID.randomUUID(),
            customerId = testCustomer.id,
            name = "X-wing",
            type = Asset.Type.STARSHIP,
            externalId = 12,
            value = 149999,
        ),
        Asset(
            id = UUID.randomUUID(),
            customerId = testCustomer.id,
            name = "Imperial shuttle",
            type = Asset.Type.STARSHIP,
            externalId = 22,
            value = 240000,
        )
    )

    private final val testData = mapOf(
        testCustomer.externalId to CustomerWithAssets(testCustomer, testAssets)
    )


    @MockBean
    lateinit var externalStarWarsInfo: ExternalStarWarsInfo

    @Autowired
    lateinit var starWarsRepository: StarWarsRepository

    @BeforeEach
    fun clearDatabase() {
        starWarsRepository.deleteAll().block()
    }


    @Test
    fun `if result is not in database, store and respond`() {

        `when`(externalStarWarsInfo.getCustomerWithAssets(1))
            .thenReturn(Mono.just(testData[1]!!))

        // Database should be empty at first
        assert(starWarsRepository.getCustomerWithAssets(1).block() == null)

        // Call should succeed with correct data
        given()
            .port(port)
            .`when`()
            .get("/customers/1")
            .then()
            .statusCode(200)
            .body("customer.name", `is`(testCustomer.name))
            .body("assets.size()", `is`(testAssets.size))

        // Database should now contain the data
        assert(starWarsRepository.getCustomerWithAssets(1).block() == testData[1]!!)
    }

    @Test
    fun `if result is in database, respond without calling external api`() {
        `when`(externalStarWarsInfo.getCustomerWithAssets(1))
            .then { throw RuntimeException("should not be called") }

        // Database should contain the data at first
        starWarsRepository.insertCustomerWithAssets(testData[1]!!).block()

        // Call should succeed with correct data
        given()
            .port(port)
            .`when`()
            .get("/customers/1")
            .then()
            .statusCode(200)
            .log().all()
            .body("customer.name", `is`(testCustomer.name))
            .body("assets.size()", `is`(testAssets.size))

        // Database should still contain the data
        assert(starWarsRepository.getCustomerWithAssets(1).block() == testData[1]!!)
    }

    fun `if result does not exist then 404`() {
        `when`(externalStarWarsInfo.getCustomerWithAssets(1))
            .thenReturn(Mono.empty())

        // Database should be empty at first
        assert(starWarsRepository.getCustomerWithAssets(1).block() == null)

        // Call should succeed with correct data
        given()
            .port(port)
            .`when`()
            .get("/customers/1")
            .then()
            .statusCode(404)
    }
}
