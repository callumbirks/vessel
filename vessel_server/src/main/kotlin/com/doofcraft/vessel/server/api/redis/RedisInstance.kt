package com.doofcraft.vessel.server.api.redis

import com.doofcraft.vessel.server.VesselConfig
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection

object RedisInstance {
    val client: RedisClient by lazy {
        val uri = RedisURI.create(VesselConfig.CONFIG.redisUri)
        RedisClient.create(uri)
    }

    fun blockingConnection(): StatefulRedisConnection<String, String> {
        return client.connect()
    }
}