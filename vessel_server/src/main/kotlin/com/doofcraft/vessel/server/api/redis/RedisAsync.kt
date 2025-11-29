package com.doofcraft.vessel.server.api.redis

interface RedisAsync {
    suspend fun get(key: String): String?
    suspend fun set(key: String, value: String)
    suspend fun setex(key: String, seconds: Long, value: String)
    suspend fun del(vararg keys: String)
    suspend fun unlink(vararg keys: String)
    suspend fun incr(key: String): Long
    suspend fun smembers(key: String): Set<String>
    suspend fun sadd(key: String, vararg members: String): Long
    suspend fun srem(key: String, vararg members: String): Long
    suspend fun sismember(key: String, member: String): Boolean
    suspend fun hget(key: String, field: String): String?
    suspend fun hset(key: String, field: String, value: String): Boolean
    suspend fun hmset(key: String, map: Map<String, String>)
    suspend fun hgetall(key: String): Map<String, String>
    suspend fun hmget(key: String, vararg fields: String): Map<String, String>
    suspend fun hexists(key: String, field: String): Boolean
    suspend fun hdel(key: String, vararg fields: String): Long
    suspend fun expire(key: String, seconds: Long)
    /// Returns null if the key does not exist. Otherwise, the key's TTL (0 for no TTL).
    suspend fun ttl(key: String): Long?
    suspend fun exists(vararg keys: String): Boolean
}