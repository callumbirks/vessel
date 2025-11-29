package com.doofcraft.vessel.server.api.redis

class AssociatedMap<ID : Any, F : Any, V : Any>(
    private val redisSupplier: () -> RedisAsync,
    val keyspace: RedisKeyspace<ID, *>,
    val name: String,
    private val fieldToString: (F) -> String,
    private val fieldFromString: (String) -> F,
    private val valueToString: (V) -> String,
    private val valueFromString: (String) -> V,
) {
    private fun map(id: ID) =
        RedisMap(redisSupplier, { keyForId(id) }, fieldToString, fieldFromString, valueToString, valueFromString)

    suspend fun set(id: ID, field: F, value: V, ttl: Long?): Boolean {
        return map(id).set(field, value, ttl)
    }

    suspend fun set(id: ID, map: Map<F, V>, ttl: Long?) {
        return map(id).set(map, ttl)
    }

    suspend fun get(id: ID, field: F): V? {
        return map(id).get(field)
    }

    suspend fun getMany(id: ID, vararg fields: F): Map<F, V> {
        return map(id).getMany(*fields)
    }

    suspend fun all(id: ID): Map<F, V> {
        return map(id).all()
    }

    suspend fun remove(id: ID, vararg fields: F): Long {
        return map(id).remove(*fields)
    }

    suspend fun delete(id: ID) {
        return map(id).delete()
    }

    private fun keyForId(id: ID): String = keyspace.keyForId(id) + ":$name"
}