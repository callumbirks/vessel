package com.doofcraft.vessel.server.api.redis

abstract class UniqueIndex<E: Any, Q: Any>(
    val redisKeyFromQuery: (Q) -> String,
    val redisKeyFromEntity: (E) -> String?,
    val ttlSeconds: (E) -> Long? = { null }
)

abstract class SetIndex<E: Any, Q: Any>(
    val redisKeyFromQuery: (Q) -> String,
    val redisKeyFromEntity: (E) -> String?,
    val ttlSeconds: (E) -> Long? = { null }
)