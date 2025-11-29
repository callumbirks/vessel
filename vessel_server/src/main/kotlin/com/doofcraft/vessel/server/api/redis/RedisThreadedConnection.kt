package com.doofcraft.vessel.server.api.redis

import com.doofcraft.vessel.common.VesselMod
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import java.util.concurrent.ConcurrentLinkedQueue

object RedisThreadedConnection {
    private val activeConnections = ConcurrentLinkedQueue<StatefulRedisConnection<String, String>>()

    private val threadRedisConnection = ThreadLocal.withInitial {
        VesselMod.LOGGER.info("Creating new Redis connection for thread: ${Thread.currentThread().name}")
        val connection = RedisInstance.client.connect()
        activeConnections.add(connection)
        connection
    }

    fun async(): RedisAsyncCommands<String, String> = threadRedisConnection.get().async()

    internal fun shutdown() {
        VesselMod.LOGGER.info("Closing Redis connections...")
        activeConnections.forEach { it.close() }
        RedisInstance.client.shutdown()
        VesselMod.LOGGER.info("All Redis connections closed.")
    }
}