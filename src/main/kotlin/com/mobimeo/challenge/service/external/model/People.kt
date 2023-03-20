package com.mobimeo.challenge.service.external.model

data class Person(
    val id: Long,
    val name: String,
    val vehicles: List<String>,
    val starships: List<String>
)
