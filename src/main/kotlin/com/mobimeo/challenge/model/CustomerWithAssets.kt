package com.mobimeo.challenge.model

data class CustomerWithAssets(
    val customer: Customer,
    val assets: List<Asset>
)
