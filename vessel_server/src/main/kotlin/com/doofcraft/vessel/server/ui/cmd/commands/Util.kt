package com.doofcraft.vessel.server.ui.cmd.commands

import com.doofcraft.vessel.server.ui.UiManager
import com.doofcraft.vessel.server.ui.cmd.UiCommand
import com.doofcraft.vessel.server.ui.cmd.UiContext
import com.doofcraft.vessel.server.ui.expr.ExprEngine
import com.doofcraft.vessel.server.ui.expr.Scope

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
        // For brevity, pass through the input; in production, evaluate expressions per field using an ExprEngine.
        // Here you would call engine.eval for each field entry with scope.value = current row.
        val engine = UiManager.service.engine

        return list.map { row ->
            val scope = Scope(
                menu = emptyMap(),
                player = mapOf("uuid" to ctx.playerUuid),
                nodeValues = ctx.state["__nodeValues__"] as? Map<String, Any?> ?: emptyMap(),
                value = row,
                state = ctx.state
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
        val list = (args["list"] as? List<*>) ?: emptyList<Any?>()
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
        val map = args["map"] as? Map<*, *> ?: return emptyList<Map<String, Any?>>()
        val keyName = (args["keysAs"]?.toString() ?: "key")
        val valName = (args["valuesAs"]?.toString() ?: "value")
        return map.entries.map { e -> mapOf(keyName to e.key, valName to e.value) }
    }
}