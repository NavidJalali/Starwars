package com.mobimeo.challenge.repository

import com.mobimeo.challenge.model.CustomerWithAssets
import reactor.core.publisher.Mono

interface StarWarsRepository: AssetRepository, CustomerRepository {
    fun getCustomerWithAssets(externalId: Long): Mono<CustomerWithAssets>
    fun insertCustomerWithAssets(customerWithAssets: CustomerWithAssets): Mono<CustomerWithAssets>
    fun deleteAll(): Mono<Void>
}