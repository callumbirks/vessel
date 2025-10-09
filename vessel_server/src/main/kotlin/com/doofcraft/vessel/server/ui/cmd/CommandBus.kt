package com.doofcraft.vessel.server.ui.cmd

import java.util.concurrent.ConcurrentHashMap

interface UiCommand {
    val id: String

    /** input is upstream node output (nullable); args is JSON from definition with templates already resolved */
    suspend fun run(ctx: UiContext, input: Any?, args: Map<String, Any?>): Any?
}

data class UiContext(
    val playerUuid: String,
    val menuId: String,
    val params: Map<String, Any?>,
    /** mutable local state per open menu (pagination, filters, etc.) */
    val state: MutableMap<String, Any> = ConcurrentHashMap()
)

object CommandBus {
    private val commands = ConcurrentHashMap<String, UiCommand>()

    fun register(cmd: UiCommand) { commands[cmd.id] = cmd }
    fun get(id: String): UiCommand = commands[id]
        ?: error("Command not found: $id")
}

class NodeCache {
    private data class Entry(val value: Any?, val expiresAt: Long)
    private val map = ConcurrentHashMap<String, Entry>()

    fun get(key: String): Any? = map[key]?.takeIf { System.currentTimeMillis() < it.expiresAt }?.value
    fun put(key: String, value: Any?, ttlMs: Long) {
        map[key] = Entry(value, System.currentTimeMillis() + ttlMs)
    }
}