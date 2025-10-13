package com.doofcraft.vessel.server.ui.cmd.commands

import com.doofcraft.vessel.server.ui.UiManager
import com.doofcraft.vessel.server.ui.cmd.CommandBus
import com.doofcraft.vessel.server.ui.cmd.UiCommand
import com.doofcraft.vessel.server.ui.cmd.UiContext
import com.doofcraft.vessel.server.ui.expr.ExprEngine
import com.doofcraft.vessel.server.ui.expr.ExprRef
import com.doofcraft.vessel.server.ui.expr.JsonTemplater
import com.doofcraft.vessel.server.ui.expr.Scope
import com.doofcraft.vessel.server.ui.expr.SimpleExprEngine
import com.doofcraft.vessel.server.ui.expr.TemplateRef
import com.doofcraft.vessel.server.ui.expr.evalDeferred

object UtilTake : UiCommand {
    override val id: String = "util.take"
    override suspend fun run(ctx: UiContext, input: Any?, args: Map<String, Any?>): Any? {
        val list = (input as? List<*>) ?: return emptyList<Any?>()
        val n = args["n"].toString().toInt()
        return list.take(n)
    }
}

object UtilMapList : UiCommand {
    override val id: String = "util.map_list"
    override suspend fun run(ctx: UiContext, input: Any?, args: Map<String, Any?>): Any? {
        val list = (input as? List<*>)?.filterIsInstance<Map<String, Any?>>().orEmpty()

        VesselMod.LOGGER.info("MAP_LIST input = $list, args = $args, ctx = $ctx")

        val fields = args["fields"] as? Map<*, *>
        val engine = UiManager.service.engine

        if (fields != null) {
            return list.mapIndexed { i, row ->
                val scope = ctx.toScope(value = row + ("index" to i))
                buildRow(fields, scope, engine)
            }
        }

        val cmdId = args["cmd"]?.toString() ?: return emptyList<Any?>()

        val cmdInput = args["input"]
        val cmdArgs = args["args"] as? Map<String, Any?>

        val cmd = CommandBus.get(cmdId)

        return list.mapIndexed { i, row ->
            val scope = ctx.toScope(value = row + ("index" to i))
            val input = when (cmdInput) {
                is String -> engine.eval(cmdInput, scope)
                else -> cmdInput.evalDeferred(engine, scope)
            }
            val args = cmdArgs?.mapValues { (key, value) ->
                if (key == "cmd") value
                else when (value) {
                    is String -> engine.eval(value, scope)
                    else -> value.evalDeferred(engine, scope)
                }
            } ?: emptyMap()

            cmd.run(ctx, input, args)
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

object UtilMapValues : UiCommand {
    override val id: String = "util.map_values"
    override suspend fun run(ctx: UiContext, input: Any?, args: Map<String, Any?>): Any? {
        val map = input as? Map<*, *> ?: return null
        val cmdId = args["cmd"]?.toString()
        val eval = args["eval"]?.toString()
        return if (cmdId != null) {
            val cmd = CommandBus.get(cmdId)
            map.mapValues { (_, value) ->
                cmd.run(ctx, value, emptyMap())
            }
        } else if (eval != null) {
            map.mapValues { (_, value) ->
                UiManager.service.engine.eval(eval, ctx.toScope(value = value))
            }
        } else {
            map
        }
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

object UtilCount : UiCommand {
    override val id: String = "util.count"
    override suspend fun run(ctx: UiContext, input: Any?, args: Map<String, Any?>): Any? {
        val list = input as? List<*> ?: emptyList<Any?>()
        val cond = args["where"]?.toString()
        val engine = UiManager.service.engine
        val count = if (cond == null) list.count() else list.count { elem ->
            val scope = ctx.toScope(value = elem)
            engine.eval(cond, scope) == true
        }
        return count
    }
}

object UtilEval : UiCommand {
    override val id: String = "util.eval"
    override suspend fun run(ctx: UiContext, input: Any?, args: Map<String, Any?>): Any? {
        val expr = args["expr"]?.toString() ?: return null
        val scope = ctx.toScope(value = input)
        return UiManager.service.engine.eval(expr, scope)
    }
}