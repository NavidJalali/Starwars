package com.mobimeo.challenge.service.starwars

import com.mobimeo.challenge.model.Customer
import com.mobimeo.challenge.model.CustomerWithAssets
import reactor.core.publisher.Mono

interface StarWars {
    fun getCustomerWithAssets(externalId: Long): Mono<CustomerWithAssets>
    fun getAllCustomers(): Mono<List<Customer>>
}
