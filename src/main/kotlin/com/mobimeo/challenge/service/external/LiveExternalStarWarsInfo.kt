package com.mobimeo.challenge.service.external

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mobimeo.challenge.model.Asset
import com.mobimeo.challenge.model.Customer
import com.mobimeo.challenge.model.CustomerWithAssets
import com.mobimeo.challenge.service.external.model.Person
import com.mobimeo.challenge.service.external.model.Property
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.netty.http.client.HttpClient
import java.util.*

@Service
final class LiveExternalStarWarsInfo(
    private val httpClient: HttpClient
) : ExternalStarWarsInfo {
    private val objectMapper = jacksonObjectMapper().apply {
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        registerKotlinModule()
    }
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val baseUrl = "https://swapi.dev/api/"

    private fun idOf(resource: Resources, rawUrl: String): Long? {
        val base = baseUrl + resource.name.lowercase() + "/"
        return Regex("""${Regex.escape(base)}(\d+)""")
            .find(rawUrl)
            ?.groupValues
            ?.getOrNull(1)
            ?.toLongOrNull()
    }

    enum class Resources {
        PEOPLE, VEHICLES, STARSHIPS;

        fun toAssetType(): Asset.Type {
            return when (this) {
                PEOPLE -> throw RuntimeException("People are not assets")
                VEHICLES -> Asset.Type.VEHICLE
                STARSHIPS -> Asset.Type.STARSHIP
            }
        }
    }

    private fun getResource(resource: Resources, id: Long): Mono<String> {
        val url = "$baseUrl${resource.name.lowercase()}/$id/"
        return httpClient.get()
            .uri(url)
            .responseSingle() {
                response, body ->
                if (response.status().code() == 200) {
                    body.map { it.toString(Charsets.UTF_8) }
                } else if (response.status().code() == 404) {
                    Mono.empty()
                } else {
                    Mono.error(RuntimeException("Error while fetching resource."))
                }
            }
    }

    private fun getAssets(
        resource: Resources,
        urls: List<String>,
        customerId: UUID
    ): Mono<List<Asset>> {
        return Flux.fromIterable(urls)
            .flatMap { rawUrl ->
                idOf(resource, rawUrl)
                    ?.let { Mono.just(it) }
                    ?: run { Mono.error(RuntimeException("Raw url read from response is not parsable.")) }
            }
            .flatMap { id ->
                getResource(resource, id)
                    .map { objectMapper.readValue<Property>(it) }
                    .map { property ->
                        Asset(
                            id = UUID.randomUUID(),
                            externalId = id,
                            name = property.name,
                            customerId = customerId,
                            type = resource.toAssetType(),
                            value = property.value() ?: 0
                        )
                    }
            }
            .collectList()
    }


    override fun getCustomerWithAssets(externalId: Long): Mono<CustomerWithAssets> {
        return getResource(Resources.PEOPLE, externalId)
            .map { objectMapper.readValue<Person>(it) }
            .flatMap { person ->
                val customer = Customer(id = UUID.randomUUID(), externalId, name = person.name)
                val vehicles =
                    getAssets(Resources.VEHICLES, person.vehicles, customer.id)
                val starships =
                    getAssets(Resources.STARSHIPS, person.starships, customer.id)

                vehicles.zipWith(starships).map { (v, s) ->
                    CustomerWithAssets(customer, v + s)
                }
            }
    }
}