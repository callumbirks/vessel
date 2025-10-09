package com.doofcraft.vessel.server.ui

import com.doofcraft.vessel.common.component.MenuButton
import com.doofcraft.vessel.server.ui.cmd.CommandBus
import com.doofcraft.vessel.server.ui.cmd.NodeCache
import com.doofcraft.vessel.server.ui.cmd.UiContext
import com.doofcraft.vessel.server.ui.data.DataExecutor
import com.doofcraft.vessel.server.ui.data.DataPlan
import com.doofcraft.vessel.server.ui.data.DataPlanner
import com.doofcraft.vessel.server.ui.expr.ExprEngine
import com.doofcraft.vessel.server.ui.expr.Scope
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
    val engine: ExprEngine,
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

    fun openMenu(player: ServerPlayer, def: MenuDefinition, params: Map<String, Any?>) {
        def.openParams?.let { req ->
            // Check all non-optional (optional meaning 'int?' or similar) params are present.
            val nonOptional = req.filterNot { (_, type) -> type.endsWith('?') }
            require(params.keys.containsAll(nonOptional.keys)) { "Missing open params" }
        }
        val plan = DataPlanner.compile(def)
        val ctx = UiContext(player.uuid.toString(), def.id, params, ConcurrentHashMap())
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
        val cmd = CommandBus.get(menuButton.cmd)
        val ctx = getContext(player)
            ?: return
        scope.launch {
            cmd.run(ctx, null, menuButton.args)
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
        val scope = Scope(
            menu = mapOf("id" to plan.def.id),
            params = ctx.params,
            player = mapOf("uuid" to ctx.playerUuid),
            nodeValues = values,
            state = ctx.state
        )
        val renderedTitle = engine.renderTemplate(plan.def.title, scope)
        val rendered = renderer.renderAll(plan.def, renderedTitle, values, ctx.state, player)

        val openMenu =
            (player.containerMenu as? GenericInventoryScreenHandler)?.container as? InventoryMenuContainer ?: run {
                if (nodes == null) {
                    // this is the first refresh, open the menu
                    val m = InventoryMenuContainer(renderedTitle, plan.def.rows, rendered.items.toMutableMap())
                    m.open(player)
                    m
                } else {
                    // this is a subsequent refresh, so the player closed the menu, return
                    return
                }
            }

        if (openMenu.name == renderedTitle) {
            // If the name has stayed the same we can just patch the items
            openMenu.patchItems(rendered.items)
        } else {
            // Re-open if the name has changed
            // TODO: Maybe we can do some Packet trickery to avoid this?
            val newMenu = InventoryMenuContainer(renderedTitle, plan.def.rows, rendered.items.toMutableMap())
            newMenu.open(player)
        }
    }
}