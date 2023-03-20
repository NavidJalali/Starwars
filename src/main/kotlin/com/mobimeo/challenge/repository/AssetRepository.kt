package com.mobimeo.challenge.repository

import com.mobimeo.challenge.model.Asset
import reactor.core.publisher.Mono

interface AssetRepository {
    fun insertAssets(assets: List<Asset>): Mono<Long>
}
