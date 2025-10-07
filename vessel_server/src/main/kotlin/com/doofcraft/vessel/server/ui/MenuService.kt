package com.doofcraft.vessel.server.ui

import com.doofcraft.vessel.common.component.MenuButton
import com.doofcraft.vessel.server.ui.cmd.CommandBus
import com.doofcraft.vessel.server.ui.cmd.NodeCache
import com.doofcraft.vessel.server.ui.cmd.UiContext
import com.doofcraft.vessel.server.ui.data.DataExecutor
import com.doofcraft.vessel.server.ui.data.DataPlan
import com.doofcraft.vessel.server.ui.data.DataPlanner
import com.doofcraft.vessel.server.ui.expr.ExprEngine
import com.doofcraft.vessel.server.ui.handler.GenericInventoryScreenHandler
import com.doofcraft.vessel.server.ui.handler.InventoryMenuContainer
import com.doofcraft.vessel.server.ui.model.MenuDefinition
import com.doofcraft.vessel.server.ui.render.WidgetRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.minecraft.server.level.ServerPlayer
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set
import kotlin.collections.toMutableMap
import kotlin.let
import kotlin.run

class MenuService(
    private val engine: ExprEngine,
    private val renderer: WidgetRenderer,
    private val executor: DataExecutor,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private data class OpenMenu(
        val def: MenuDefinition,
        val plan: DataPlan,
        val ctx: UiContext,
        val cache: NodeCache,
        val job: Job,
    )

    private val open = ConcurrentHashMap<String, OpenMenu>() // playerUuid -> OpenMenu

    fun openMenu(player: ServerPlayer, def: MenuDefinition, params: Map<String, String>) {
        val plan = DataPlanner.compile(def)
        val ctx = UiContext(player.uuid.toString(), def.id, ConcurrentHashMap())
        def.openParams?.let { req ->
            require(params.keys.containsAll(req.keys)) { "Missing open params" }
        }
        val cache = NodeCache()
        val playerUuid = player.uuid.toString()

        val job = scope.launch {
            refreshOnce(player, plan, ctx, cache)

            // Periodic refresh
            val interval = def.refresh?.intervalMs ?: 0L
            if (interval > 0) {
                while (isActive) {
                    delay(interval)
                    refreshOnce(player, plan, ctx, cache, nodes = def.refresh!!.nodes)
                }
            }
        }
        open[playerUuid]?.job?.cancel()
        open[playerUuid] = OpenMenu(def, plan, ctx, cache, job)
    }

    fun closeMenu(player: ServerPlayer) {
        open.remove(player.uuid.toString())?.job?.cancel()
    }

    fun clickButton(player: ServerPlayer, menuButton: MenuButton) {
        when (menuButton.action) {
            MenuButton.Action.CLOSE -> player.closeContainer()
            MenuButton.Action.NAVIGATE -> {
                val target = menuButton.data["target"]
                if (target != null) {
                    val menu = MenuRegistry.getOrThrow(target)
                    openMenu(player, menu, menuButton.data)
                } else {
                    player.closeContainer()
                }
            }
            MenuButton.Action.ACCEPT -> {
                val cmdId = menuButton.data["cmd"]
                    ?: return
                val argsJson = menuButton.data["args"]
                val ctx = getContext(player)
                    ?: return
                val cmd = CommandBus.get(cmdId)
                scope.launch {
                    cmd.run(ctx, null, argsJson)
                }
            }
        }
    }

    fun getContext(player: ServerPlayer): UiContext? {
        return open[player.uuid.toString()]?.ctx
    }

    private suspend fun refreshOnce(
        player: ServerPlayer, plan: DataPlan, ctx: UiContext, cache: NodeCache, nodes: List<String>? = null
    ) {
        // TODO: if nodes == null -> full execution; else reuse previous values and recompute only what's necessary
        val values = executor.executeAll(plan, ctx, cache)
        val rendered = renderer.renderAll(plan.def, values, ctx.state, player)

        val openMenu =
            (player.containerMenu as? GenericInventoryScreenHandler)?.container as? InventoryMenuContainer ?: run {
                if (nodes == null) {
                    // this is the first refresh, open the menu
                    val m = InventoryMenuContainer(plan.def.title, rendered.items.toMutableMap())
                    m.open(player)
                    m
                } else {
                    // this is a subsequent refresh, so the player closed the menu, return
                    return
                }
            }

        if (openMenu.name == plan.def.title) {
            // If the name has stayed the same we can just patch the items
            openMenu.patchItems(rendered.items)
        } else {
            // Re-open if the name has changed
            // TODO: Maybe we can do some Packet trickery to avoid this?
            val newMenu = InventoryMenuContainer(plan.def.title, rendered.items.toMutableMap())
            newMenu.open(player)
        }
    }
}