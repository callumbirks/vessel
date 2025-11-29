package com.doofcraft.vessel.server.api.redis

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class RedisEntityStore<ID: Any, E: Any>(
    private val redisSupplier: () -> RedisAsync,
    private val keyspace: RedisKeyspace<ID, E>,
    private val uniqueIndices: List<UniqueIndex<E, *>> = emptyList(),
    private val setIndices: List<SetIndex<E, *>> = emptyList(),
) {
    suspend fun findById(id: ID): E? {
        val redis = redisSupplier()
        val raw = redis.get(keyspace.keyForId(id)) ?: return null
        return keyspace.deserialize(raw)
    }

    suspend fun save(entity: E, id: ID) {
        val redis = redisSupplier()
        val key = keyspace.keyForId(id)
        val ttl = keyspace.ttlSeconds(entity)

        val json = keyspace.serialize(entity)
        if (ttl != null) redis.setex(key, ttl, json) else redis.set(key, json)

        uniqueIndices.forEach { idx ->
            @Suppress("UNCHECKED_CAST")
            (idx as UniqueIndex<E, Any>)
            val idxKey = idx.redisKeyFromEntity(entity) ?: return@forEach
            val idxTtl = idx.ttlSeconds(entity) ?: ttl
            if (idxTtl != null) redis.setex(idxKey, idxTtl, keyspace.idToString(id))
            else redis.set(idxKey, keyspace.idToString(id))
        }

        setIndices.forEach { idx ->
            @Suppress("UNCHECKED_CAST")
            (idx as SetIndex<E, Any>)
            val idxKey = idx.redisKeyFromEntity(entity) ?: return@forEach
            redis.sadd(idxKey, keyspace.idToString(id))
            val idxTtl = idx.ttlSeconds(entity) ?: ttl
            if (idxTtl != null) redis.expire(idxKey, idxTtl)
        }
    }

    suspend fun delete(id: ID) {
        val entity = findById(id) ?: return
        val redis = redisSupplier()
        val key = keyspace.keyForId(id)

        redis.unlink(key)

        uniqueIndices.forEach { idx ->
            @Suppress("UNCHECKED_CAST")
            (idx as UniqueIndex<E, Any>)
            val idxKey = idx.redisKeyFromEntity(entity) ?: return@forEach
            redis.unlink(idxKey)
        }

        setIndices.forEach { idx ->
            @Suppress("UNCHECKED_CAST")
            (idx as SetIndex<E, Any>)
            val idxKey = idx.redisKeyFromEntity(entity) ?: return@forEach
            redis.srem(idxKey, keyspace.idToString(id))
        }
    }

    suspend fun <Q : Any> findByUnique(index: UniqueIndex<E, Q>, query: Q): E? {
        val redis = redisSupplier()
        val key = index.redisKeyFromQuery(query)
        val idString = redis.get(key) ?: return null
        val id = keyspace.idFromString(idString)
        return findById(id)
    }

    suspend fun <Q : Any> findAllBySetIndex(index: SetIndex<E, Q>, query: Q): List<E> = coroutineScope {
        val redis = redisSupplier()
        val idxKey = index.redisKeyFromQuery(query)
        val members = redis.smembers(idxKey)
        members.map { idString ->
            async {
                findById(keyspace.idFromString(idString))
            }
        }.awaitAll().filterNotNull()
    }

    suspend fun exists(id: ID): Boolean {
        val redis = redisSupplier()
        val key = keyspace.keyForId(id)
        return redis.exists(key)
    }
}