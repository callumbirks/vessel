package com.doofcraft.vessel.server.api.redis

class AssociatedSet<ID : Any, V : Any>(
    private val redisSupplier: () -> RedisAsync,
    val keyspace: RedisKeyspace<ID, *>,
    val name: String,
    private val valueToString: (V) -> String,
    private val valueFromString: (String) -> V,
) {
    suspend fun add(id: ID, ttl: Long?, vararg values: V): Boolean {
        val key = keyForId(id)
        val redis = redisSupplier()
        val result = redis.sadd(key, *values.map { valueToString(it) }.toTypedArray())
        ttl?.let { redis.expire(key, it) }
        return result > 0L
    }

    suspend fun add(id: ID, ttl: Long?, values: Collection<V>): Boolean {
        val key = keyForId(id)
        val redis = redisSupplier()
        val result = redis.sadd(key, *values.map { valueToString(it) }.toTypedArray())
        ttl?.let { redis.expire(key, it) }
        return result > 0L
    }

    suspend fun contains(id: ID, value: V): Boolean {
        val key = keyForId(id)
        val redis = redisSupplier()
        return redis.sismember(key, valueToString(value))
    }

    suspend fun remove(id: ID, vararg values: V): Boolean {
        val key = keyForId(id)
        val redis = redisSupplier()
        return redis.srem(key, *values.map { valueToString(it) }.toTypedArray()) > 0L
    }

    suspend fun remove(id: ID, values: Collection<V>): Boolean {
        val key = keyForId(id)
        val redis = redisSupplier()
        return redis.srem(key, *values.map { valueToString(it) }.toTypedArray()) > 0L
    }

    suspend fun members(id: ID): Set<V> {
        val key = keyForId(id)
        val redis = redisSupplier()
        return redis.smembers(key).map(valueFromString).toSet()
    }

    suspend fun delete(id: ID) {
        val redis = redisSupplier()
        redis.unlink(keyForId(id))
    }

    private fun keyForId(id: ID): String = keyspace.keyForId(id) + ":$name"
}