package com.mobimeo.challenge.service.starwars

import com.mobimeo.challenge.model.Customer
import com.mobimeo.challenge.model.CustomerWithAssets
import com.mobimeo.challenge.repository.StarWarsRepository
import com.mobimeo.challenge.service.external.ExternalStarWarsInfo
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
final class LiveStarWars(
    private val repository: StarWarsRepository,
    private val externalStarWarsInfo: ExternalStarWarsInfo
) : StarWars {
    private fun insertCustomerWithAssets(customerWithAssets: CustomerWithAssets): Mono<CustomerWithAssets> {
        return repository.insertCustomerWithAssets(customerWithAssets)
    }

    override fun getCustomerWithAssets(externalId: Long): Mono<CustomerWithAssets> {
        return repository.getCustomerWithAssets(externalId)
            .flatMap {
                println("BOOGER")
                Mono.just(it)
            }
            .switchIfEmpty {
                externalStarWarsInfo
                    .getCustomerWithAssets(externalId)
                    .flatMap { insertCustomerWithAssets(it) }
            }
    }

    override fun getAllCustomers(): Mono<List<Customer>> {
        return repository.getAllCustomers()
    }
}