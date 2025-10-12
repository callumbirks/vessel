package com.doofcraft.vessel.server.ui.cmd.commands

import com.doofcraft.vessel.server.ui.UiManager
import com.doofcraft.vessel.server.ui.cmd.UiCommand
import com.doofcraft.vessel.server.ui.cmd.UiContext
import com.doofcraft.vessel.server.ui.expr.ExprEngine
import com.doofcraft.vessel.server.ui.expr.Scope
import com.doofcraft.vessel.server.ui.expr.SimpleExprEngine

object UtilTake : UiCommand {
    override val id: String = "util.take"
    override suspend fun run(ctx: UiContext, input: Any?, args: Map<String, Any?>): Any? {
        val list = (input as? List<*>) ?: return emptyList<Any?>()
        val n = args["n"].toString().toInt()
        return list.take(n)
    }
}

object UtilMapList : UiCommand {
    override val id: String = "util.map"
    override suspend fun run(ctx: UiContext, input: Any?, args: Map<String, Any?>): Any? {
        val list = (input as? List<*>)?.filterIsInstance<Map<String, Any?>>().orEmpty()
        val fields = args["fields"] as? Map<*, *> ?: emptyMap<Any?, Any?>()

        val engine = UiManager.service.engine

        return list.mapIndexed { i, row ->
            val scope = Scope(
                menu = mapOf("id" to ctx.menuId),
                params = ctx.params,
                player = mapOf("uuid" to ctx.playerUuid),
                nodeValues = ctx.nodeValues,
                state = ctx.state,
                value = row + ("index" to i)
            )
            buildRow(fields, scope, engine)
        }
    }

    private fun buildRow(
        fields: Map<*, *>, scope: Scope, engine: ExprEngine
    ): Map<String, Any?> {
        val out = HashMap<String, Any?>(fields.size)
        for ((k, spec) in fields) {
            val key = k.toString()
            val value = when (spec) {
                is String -> engine.eval(spec, scope)          // expression string
                is Number, is Boolean -> spec                  // literal
                is Map<*, *>, is List<*> -> spec               // pass-through structured literal
                null -> null
                else -> spec.toString()
            }
            out[key] = value
        }
        return out
    }
}

object UtilPage : UiCommand {
    override val id: String = "util.page"
    override suspend fun run(ctx: UiContext, input: Any?, args: Map<String, Any?>): Any? {
        val list = input as? List<*> ?: emptyList<Any?>()
        val page = (args["page"] as? Number)?.toInt() ?: 0
        val size = (args["size"] as? Number)?.toInt() ?: 9
        val from = (page * size).coerceAtLeast(0)
        val to = (from + size).coerceAtMost(list.size)
        val slice = if (from < to) list.subList(from, to) else emptyList()
        return mapOf(
            "items" to slice, "page" to page, "size" to size, "hasNext" to (to < list.size), "hasPrev" to (page > 0)
        )
    }
}

object UtilEntries : UiCommand {
    override val id: String = "util.entries"
    override suspend fun run(ctx: UiContext, input: Any?, args: Map<String, Any?>): Any? {
        val map = input as? Map<*, *> ?: return emptyList<Map<String, Any?>>()
        val keyName = (args["keysAs"]?.toString() ?: "key")
        val valName = (args["valuesAs"]?.toString() ?: "value")
        return map.entries.map { e -> mapOf(keyName to e.key, valName to e.value) }
    }
}

object UtilLookup : UiCommand {
    override val id: String = "util.lookup"
    override suspend fun run(ctx: UiContext, input: Any?, args: Map<String, Any?>): Any? {
        val keyAny = args["key"] ?: return null
        val default = args["default"]
        return when (input) {
            is Map<*, *> -> SimpleExprEngine.Util.smartGet(input, keyAny) ?: default
            is List<*> -> {
                val intKey = keyAny as? Int ?: keyAny.toString().toIntOrNull()
                if (intKey != null) input[intKey] ?: default
                else default
            }
            is Set<*> -> {
                input.contains(keyAny)
            }
            else -> default
        }
    }
}