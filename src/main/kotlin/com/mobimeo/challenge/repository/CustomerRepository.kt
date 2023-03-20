package com.mobimeo.challenge.repository

import com.mobimeo.challenge.model.Customer
import reactor.core.publisher.Mono

interface CustomerRepository {
    fun insertCustomer(customer: Customer): Mono<Customer>

    fun getAllCustomers(): Mono<List<Customer>>
}
