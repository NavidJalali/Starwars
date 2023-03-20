package com.mobimeo.challenge.service.external.model

data class Property(
    val id: Long,
    val name: String,
    val costInCredits: String,
) {
    fun value(): Long? {
        return costInCredits.toLongOrNull()
    }
}
