package com.doofcraft.vessel.server.ui

import com.doofcraft.vessel.server.ui.cmd.CommandBus
import com.doofcraft.vessel.server.ui.cmd.commands.MapList
import com.doofcraft.vessel.server.ui.cmd.commands.Take
import com.doofcraft.vessel.server.ui.data.DataExecutor
import com.doofcraft.vessel.server.ui.expr.SimpleExprEngine
import com.doofcraft.vessel.server.ui.render.WidgetRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object UiManager {
    lateinit var service: MenuService

    private val BUILTIN_COMMANDS = listOf(Take, MapList)

    fun register(scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)) {
        BUILTIN_COMMANDS.forEach(CommandBus::register)
        val engine = SimpleExprEngine()
        service = MenuService(
            engine = engine, renderer = WidgetRenderer(engine), executor = DataExecutor(engine), scope
        )
    }
}