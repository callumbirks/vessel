package com.doofcraft.vessel.server.api.redis

import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.future.await

class LettuceRedisAsync(
    private val commands: RedisAsyncCommands<String, String>
) : RedisAsync {
    override suspend fun get(key: String): String? = commands.get(key).await()

    override suspend fun set(key: String, value: String) {
        commands.set(key, value).await()
    }

    override suspend fun setex(key: String, seconds: Long, value: String) {
        commands.setex(key, seconds, value).await()
    }

    override suspend fun del(vararg keys: String) {
        commands.del(*keys).await()
    }

    override suspend fun unlink(vararg keys: String) {
        commands.unlink(*keys).await()
    }

    override suspend fun incr(key: String): Long {
        return commands.incr(key).await()
    }

    override suspend fun smembers(key: String): Set<String> = commands.smembers(key).await()

    override suspend fun sadd(key: String, vararg members: String): Long {
        return commands.sadd(key, *members).await()
    }

    override suspend fun srem(key: String, vararg members: String): Long {
        return commands.srem(key, *members).await()
    }

    override suspend fun sismember(key: String, member: String): Boolean {
        return commands.sismember(key, member).await()
    }

    override suspend fun hget(key: String, field: String): String? = commands.hget(key, field).await()

    override suspend fun hexists(key: String, field: String): Boolean = commands.hexists(key, field).await()

    override suspend fun hset(key: String, field: String, value: String): Boolean {
        return commands.hset(key, field, value).await()
    }

    override suspend fun hmset(key: String, map: Map<String, String>) {
        commands.hmset(key, map).await()
    }

    override suspend fun hgetall(key: String): Map<String, String> = commands.hgetall(key).await()

    override suspend fun hmget(
        key: String, vararg fields: String
    ): Map<String, String> = commands.hmget(key, *fields).await().mapNotNull {
        if (it.isEmpty) null
        else it.key to it.value
    }.toMap()

    override suspend fun hdel(key: String, vararg fields: String): Long {
        return commands.hdel(key, *fields).await()
    }

    override suspend fun expire(key: String, seconds: Long) {
        commands.expire(key, seconds).await()
    }

    override suspend fun ttl(key: String): Long? {
        val ttl = commands.ttl(key).await()
        return when (ttl) {
            -2L -> null
            -1L -> 0L
            else -> ttl
        }
    }

    override suspend fun exists(vararg keys: String): Boolean = commands.exists(*keys).await() > 0L
}