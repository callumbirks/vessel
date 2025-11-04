package com.doofcraft.vessel.server.ui

import com.doofcraft.vessel.server.api.events.VesselEvents
import com.doofcraft.vessel.server.ui.cmd.CommandBus
import com.doofcraft.vessel.server.ui.cmd.commands.*
import com.doofcraft.vessel.server.ui.data.DataExecutor
import com.doofcraft.vessel.server.ui.expr.SimpleExprEngine
import com.doofcraft.vessel.server.ui.render.PlayerHeadUiComponentMapper
import com.doofcraft.vessel.server.ui.render.UiComponentMappers
import com.doofcraft.vessel.server.ui.render.WidgetRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.minecraft.server.level.ServerPlayer

object UiManager {
    lateinit var service: MenuService

    private val BUILTIN_COMMANDS = listOf(
        UtilTake,
        UtilMapList,
        UtilMapValues,
        UtilPage,
        UtilEntries,
        UtilLookup,
        UtilCount,
        UtilEval,
        UiNavigate,
        UiClose,
        UiSetState,
        UiRefresh
    )

    private val BUILTIN_COMP_MAPPERS = listOf(PlayerHeadUiComponentMapper)

    fun register(scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)) {
        BUILTIN_COMMANDS.forEach(CommandBus::register)
        BUILTIN_COMP_MAPPERS.forEach(UiComponentMappers::register)

        val engine = SimpleExprEngine()
        service = MenuService(
            engine = engine, renderer = WidgetRenderer(engine), executor = DataExecutor(engine), scope
        )
        VesselEvents.CONTAINER_MENU_OPENED.subscribe { event ->
            service.onMenuOpened(event.player, event.containerId)
        }
        VesselEvents.CONTAINER_MENU_CLOSED.subscribe { event ->
            service.closeMenu(event.player, event.containerId)
        }
    }

    fun open(menuName: String, player: ServerPlayer, params: Map<String, Any?>) {
        val menu = MenuRegistry.get(menuName)
        if (menu == null) {
            VesselMod.LOGGER.error("Failed to open menu. No such menu '$menuName'")
            return
        }
        service.openMenu(player, menu, params)
    }
}