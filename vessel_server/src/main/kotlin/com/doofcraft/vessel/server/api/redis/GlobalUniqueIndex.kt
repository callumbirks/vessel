package com.doofcraft.vessel.server.api.redis

class GlobalUniqueIndex<P : Any, ID : Any>(
    private val redisSupplier: () -> RedisAsync,
    private val keyForPrincipal: (P) -> String,
    private val idToString: (ID) -> String,
    private val idFromString: (String) -> ID
) {
    suspend fun set(principal: P, id: ID, ttl: Long?) {
        val redis = redisSupplier()
        val key = keyForPrincipal(principal)
        if (ttl != null) redis.setex(key, ttl, idToString(id))
        else redis.set(key, idToString(id))
    }

    suspend fun get(principal: P): ID? {
        val redis = redisSupplier()
        val raw = redis.get(keyForPrincipal(principal)) ?: return null
        return idFromString(raw)
    }

    suspend fun remove(vararg principals: P) {
        val redis = redisSupplier()
        redis.unlink(*principals.map(keyForPrincipal).toTypedArray())
    }

    suspend fun remove(principals: Collection<P>) {
        val redis = redisSupplier()
        redis.unlink(*principals.map(keyForPrincipal).toTypedArray())
    }
}