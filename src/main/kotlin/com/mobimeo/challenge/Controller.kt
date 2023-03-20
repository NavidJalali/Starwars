package com.mobimeo.challenge

import com.mobimeo.challenge.model.CustomerWithAssets
import com.mobimeo.challenge.service.starwars.StarWars
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller(
    private val starWarsService: StarWars
) {

    @GetMapping("/customers/{externalId}")
    suspend fun findCustomerWithAssets(
        @PathVariable("externalId")
        externalId: Long
    ): ResponseEntity<CustomerWithAssets> {
        return starWarsService
            .getCustomerWithAssets(externalId)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .awaitSingle()
    }
}
