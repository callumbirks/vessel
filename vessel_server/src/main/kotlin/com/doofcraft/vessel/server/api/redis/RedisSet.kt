package com.doofcraft.vessel.server.api.redis

class RedisSet<V: Any>(
    private val redisSupplier: () -> RedisAsync,
    private val key: () -> String,
    private val valueToString: (V) -> String,
    private val valueFromString: (String) -> V,
) {
    suspend fun add(ttl: Long?, vararg values: V): Boolean {
        val key = key()
        val redis = redisSupplier()
        val result = redis.sadd(key, *values.map { valueToString(it) }.toTypedArray())
        ttl?.let { redis.expire(key, it) }
        return result > 0L
    }

    suspend fun add(ttl: Long?, values: Collection<V>): Boolean {
        val key = key()
        val redis = redisSupplier()
        val result = redis.sadd(key, *values.map { valueToString(it) }.toTypedArray())
        ttl?.let { redis.expire(key, it) }
        return result > 0L
    }

    suspend fun contains(value: V): Boolean {
        val key = key()
        val redis = redisSupplier()
        return redis.sismember(key, valueToString(value))
    }

    suspend fun remove(vararg values: V): Boolean {
        val key = key()
        val redis = redisSupplier()
        return redis.srem(key, *values.map { valueToString(it) }.toTypedArray()) > 0L
    }

    suspend fun remove(values: Collection<V>): Boolean {
        val key = key()
        val redis = redisSupplier()
        return redis.srem(key, *values.map { valueToString(it) }.toTypedArray()) > 0L
    }

    suspend fun members(): Set<V> {
        val key = key()
        val redis = redisSupplier()
        return redis.smembers(key).map(valueFromString).toSet()
    }

    suspend fun delete() {
        val redis = redisSupplier()
        redis.unlink(key())
    }
}