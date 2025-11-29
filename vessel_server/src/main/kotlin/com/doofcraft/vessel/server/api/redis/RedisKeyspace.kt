package com.doofcraft.vessel.server.api.redis

interface RedisKeyspace<ID: Any, E: Any> {
    val name: String
    fun idToString(id: ID): String
    fun idFromString(s: String): ID

    fun serialize(entity: E): String
    fun deserialize(raw: String): E

    fun ttlSeconds(entity: E): Long?
}

fun <ID: Any> RedisKeyspace<ID, *>.keyForId(id: ID): String = "$name:${idToString(id)}"