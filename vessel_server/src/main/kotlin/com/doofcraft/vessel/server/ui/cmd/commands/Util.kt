package com.doofcraft.vessel.server.ui.cmd.commands

import com.doofcraft.vessel.server.ui.cmd.UiCommand
import com.doofcraft.vessel.server.ui.cmd.UiContext
import kotlin.collections.get

object Take : UiCommand {
    override val id: String = "util.take"
    override suspend fun run(ctx: UiContext, input: Any?, args: Any?): Any? {
        val list = (input as? List<*>) ?: return emptyList<Any?>()
        val n = (args as Map<*, *>)["n"].toString().toInt()
        return list.take(n)
    }
}

object MapList : UiCommand {
    override val id: String = "util.map"
    override suspend fun run(ctx: UiContext, input: Any?, args: Any?): Any? {
        val list = (input as? List<Map<String, Any?>>) ?: return emptyList<Any?>()
        val fields = (args as Map<*, *>)["fields"] as Map<*, *>
        // For brevity, pass through the input; in production, evaluate expressions per field using an ExprEngine.
        // Here you would call engine.eval for each field entry with scope.value = current row.
        return list // stub
    }
}