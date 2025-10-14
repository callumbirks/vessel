package com.doofcraft.vessel.server.ui.cmd

import com.doofcraft.vessel.server.ui.UiManager
import com.doofcraft.vessel.server.ui.expr.Scope
import com.doofcraft.vessel.server.ui.expr.evalDeferred
import java.util.concurrent.ConcurrentHashMap

interface UiCommand : EvalPolicy {
    val id: String

    /** input is upstream node output (nullable); args is JSON from definition with templates already resolved */
    suspend fun run(ctx: UiContext, input: Any?, args: Map<String, Any?>): Any?
}

interface EvalPolicy {
    /** If true, CommandBus will skip evaluating deferred expressions in the input. */
    fun deferInput(): Boolean = false

    /** Keys for args which CommandBus should not evaluate deferred expressions of. */
    fun deferArgKeys(): Set<String> = emptySet()
}

data class UiContext(
    val playerUuid: String, val menuId: String, val params: Map<String, Any?>, val nodeValues: MutableMap<String, Any>,
    /** mutable local state per open menu (pagination, filters, etc.) */
    val state: MutableMap<String, Any>
) {
    fun toScope() = Scope(
        menu = mapOf("id" to menuId),
        params = params,
        player = mapOf("uuid" to playerUuid),
        nodeValues = nodeValues,
        state = state
    )

    fun toScope(value: Any?) = Scope(
        menu = mapOf("id" to menuId),
        params = params,
        player = mapOf("uuid" to playerUuid),
        nodeValues = nodeValues,
        state = state,
        value = value
    )
}

object CommandBus {
    private val commands = ConcurrentHashMap<String, UiCommand>()

    fun register(cmd: UiCommand) {
        commands[cmd.id] = cmd
    }

    private fun get(id: String): UiCommand = commands[id] ?: error("Command not found: $id")

    /**
     * Run the command with given id.
     * Will eval any deferred input or arguments (see `DeferredExpr`).
     */
    suspend fun run(
        id: String, ctx: UiContext, input: Any?, args: Map<String, Any?>, scope: Scope = ctx.toScope()
    ): Any? {
        val cmd = get(id)
        val engine = UiManager.service.engine
        val cmdInput = if (!cmd.deferInput()) input.evalDeferred(engine, scope) else input
        val cmdArgs = args.mapValues { (key, value) ->
            if (cmd.deferArgKeys().contains(key)) {
                value
            } else {
                value.evalDeferred(engine, scope)
            }
        }
        return cmd.run(ctx, cmdInput, cmdArgs)
    }
}

class NodeCache {
    private data class Entry(val value: Any?, val expiresAt: Long)

    private val map = ConcurrentHashMap<String, Entry>()

    fun get(key: String): Any? = map[key]?.takeIf { System.currentTimeMillis() < it.expiresAt }?.value
    fun put(key: String, value: Any?, ttlMs: Long) {
        map[key] = Entry(value, System.currentTimeMillis() + ttlMs)
    }
}