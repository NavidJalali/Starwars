package com.mobimeo.challenge.service.external

import com.mobimeo.challenge.model.CustomerWithAssets
import reactor.core.publisher.Mono

interface ExternalStarWarsInfo {
    fun getCustomerWithAssets(externalId: Long): Mono<CustomerWithAssets>
}
