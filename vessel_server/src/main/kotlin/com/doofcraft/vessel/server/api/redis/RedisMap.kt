package com.doofcraft.vessel.server.api.redis

class RedisMap<F: Any, V: Any>(
    private val redisSupplier: () -> RedisAsync,
    private val key: () -> String,
    private val fieldToString: (F) -> String,
    private val fieldFromString: (String) -> F,
    private val valueToString: (V) -> String,
    private val valueFromString: (String) -> V,
) {
    suspend fun set(field: F, value: V, ttl: Long?): Boolean {
        val key = key()
        val redis = redisSupplier()
        val isNew = redis.hset(key, fieldToString(field), valueToString(value))
        ttl?.let { redis.expire(key, it) }
        return isNew
    }

    suspend fun set(map: Map<F, V>, ttl: Long?) {
        val key = key()
        val redis = redisSupplier()
        redis.hmset(key, map.mapKeys { (k, _) -> fieldToString(k) }.mapValues { (_, v) -> valueToString(v) })
    }

    suspend fun get(field: F): V? {
        val key = key()
        val redis = redisSupplier()
        val value = redis.hget(key, fieldToString(field)) ?: return null
        return valueFromString(value)
    }

    suspend fun getMany(vararg fields: F): Map<F, V> {
        val key = key()
        val redis = redisSupplier()
        val entries = redis.hmget(key, *fields.map(fieldToString).toTypedArray())
        return entries.mapKeys { (k, _) -> fieldFromString(k) }.mapValues { (_, v) -> valueFromString(v) }
    }

    suspend fun contains(field: F): Boolean {
        val key = key()
        val redis = redisSupplier()
        return redis.hexists(key, fieldToString(field))
    }

    suspend fun all(): Map<F, V> {
        val key = key()
        val redis = redisSupplier()
        return redis.hgetall(key).mapKeys { (k, _) -> fieldFromString(k) }.mapValues { (_, v) -> valueFromString(v) }
    }

    suspend fun remove(vararg fields: F): Long {
        val redis = redisSupplier()
        return redis.hdel(key(), *fields.map(fieldToString).toTypedArray())
    }

    suspend fun delete() {
        val redis = redisSupplier()
        redis.unlink(key())
    }
}