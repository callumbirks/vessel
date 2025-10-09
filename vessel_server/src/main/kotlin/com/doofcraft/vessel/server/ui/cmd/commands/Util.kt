package com.doofcraft.vessel.server.ui.cmd.commands

import com.doofcraft.vessel.server.ui.UiManager
import com.doofcraft.vessel.server.ui.cmd.UiCommand
import com.doofcraft.vessel.server.ui.cmd.UiContext
import com.doofcraft.vessel.server.ui.expr.ExprEngine
import com.doofcraft.vessel.server.ui.expr.Scope

object Take : UiCommand {
    override val id: String = "util.take"
    override suspend fun run(ctx: UiContext, input: Any?, args: Map<String, Any?>): Any? {
        val list = (input as? List<*>) ?: return emptyList<Any?>()
        val n = args["n"].toString().toInt()
        return list.take(n)
    }
}

object MapList : UiCommand {
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