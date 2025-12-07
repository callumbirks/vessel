package com.doofcraft.vessel.server

import com.doofcraft.vessel.common.reactive.SimpleObservable
import com.doofcraft.vessel.common.util.vesselResource
import com.doofcraft.vessel.server.api.config.ConfigFactory
import com.doofcraft.vessel.server.api.data.Result
import io.lettuce.core.RedisURI
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class VesselConfig(@SerialName("redis_uri") val redisUri: String?) {
    companion object : ConfigFactory<VesselConfig> {
        override val id = vesselResource("config")
        override val observable = SimpleObservable<VesselConfig>()
        override val json = Json { ignoreUnknownKeys }
        override val serializer = serializer()

        override lateinit var CONFIG: VesselConfig

        override fun reload(config: VesselConfig) {
            observable.emit(config)
        }

        override fun validate(config: VesselConfig): Result<Unit> {
            if (config.redisUri != null) {
                try {
                    RedisURI.create(config.redisUri)
                } catch (e: Exception) {
                    return Result.failure("Could not parse Redis URI: ${e.message}")
                }
            }
            return Result.success(Unit)
        }

        override fun default() = VesselConfig(redisUri = "redis://127.0.0.1:6379")
    }
}
