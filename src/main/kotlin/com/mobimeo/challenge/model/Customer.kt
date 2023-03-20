package com.mobimeo.challenge.model

import java.util.UUID

data class Customer(
    val id: UUID,
    val externalId: Long,
    val name: String
) {

    companion object {
        fun fromMap(map: Map<String, Any>): Customer? {
            return map["customer_id"]?.let { id ->
                map["customer_external_id"]?.let { externalId ->
                    map["customer_name"]?.let { name ->
                        Customer(
                            id = id as UUID,
                            externalId = externalId as Long,
                            name = name as String
                        )
                    }
                }
            }
        }

        fun unsafeFromMap(map: Map<String, Any>): Customer {
            return fromMap(map)!!
        }
    }
}
