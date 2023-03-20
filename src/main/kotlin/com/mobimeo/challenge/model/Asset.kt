package com.mobimeo.challenge.model

import java.util.UUID

data class Asset(
    val id: UUID,
    val externalId: Long,
    val name: String,
    val customerId: UUID,
    val type: Type,
    val value: Long
) {
    enum class Type {
        STARSHIP, VEHICLE
    }

    companion object {
        // Ah yes, the pyramid of doom. No for scala for comprehension to save me here.
        fun fromMap(map: Map<String, Any>): Asset? {
            return map["asset_id"]?.let { id ->
                map["asset_external_id"]?.let { externalId ->
                    map["asset_name"]?.let { name ->
                        map["asset_customer_id"]?.let { customerId ->
                            map["asset_type"]?.let { type ->
                                map["asset_value"]?.let { value ->
                                    Asset(
                                        id = id as UUID,
                                        externalId = externalId as Long,
                                        name = name as String,
                                        customerId = customerId as UUID,
                                        type = Type.valueOf(type as String),
                                        value = value as Long
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }


        fun unsafeFromMap(map: Map<String, Any>): Asset {
            return fromMap(map)!!
        }
    }
}
