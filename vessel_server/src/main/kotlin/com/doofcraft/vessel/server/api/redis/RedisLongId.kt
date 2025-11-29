package com.doofcraft.vessel.server.api.redis

class RedisLongId(
    private val redisSupplier: () -> RedisAsync,
    val key: String,
) {
    suspend fun next(): Long {
        val redis = redisSupplier()
        return redis.incr(key)
    }
}