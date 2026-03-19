package com.doofcraft.vessel.server.ui

import com.doofcraft.vessel.common.VesselMod
import com.doofcraft.vessel.common.component.MenuButton
import com.doofcraft.vessel.server.api.async.VesselAsync
import com.doofcraft.vessel.server.ui.cmd.CommandBus
import com.doofcraft.vessel.server.ui.cmd.NodeCache
import com.doofcraft.vessel.server.ui.cmd.UiContext
import com.doofcraft.vessel.server.ui.data.DataExecutor
import com.doofcraft.vessel.server.ui.data.DataPlan
import com.doofcraft.vessel.server.ui.data.DataPlanner
import com.doofcraft.vessel.server.ui.expr.ExprEngine
import com.doofcraft.vessel.server.ui.expr.JsonTemplater
import com.doofcraft.vessel.server.ui.expr.Scope
import com.doofcraft.vessel.server.ui.handler.GenericInventoryScreenHandler
import com.doofcraft.vessel.server.ui.handler.InventoryMenuContainer
import com.doofcraft.vessel.server.ui.model.MenuDefinition
import com.doofcraft.vessel.server.ui.render.WidgetRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
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
        val syncTracker: MenuSessionSyncTracker = MenuSessionSyncTracker(),
        var job: Job? = null,
    )

    private val open = ConcurrentHashMap<String, OpenMenu>() // playerUuid -> OpenMenu
    private val locks = ConcurrentHashMap<String, Mutex>()
    private fun lockFor(playerUuid: String) = locks.computeIfAbsent(playerUuid) { Mutex() }
    private suspend fun <T> withPlayerLock(playerUuid: String, action: suspend () -> T): T = lockFor(playerUuid).withLock {
        action()
    }
    private fun launchPlayerLifecycle(playerUuid: String, action: suspend () -> Unit) = scope.launch {
        withPlayerLock(playerUuid, action)
    }
    private suspend fun <T> onMainThread(action: suspend () -> T): T = VesselAsync.runOnMainThread(action)

    fun openMenu(player: ServerPlayer, def: MenuDefinition, params: Map<String, Any?>) {
        def.openParams?.let { req ->
            // Check all non-optional (optional meaning 'int?' or similar) params are present.
            val nonOptional = req.filterNot { (_, type) -> type.endsWith('?') }
            require(params.keys.containsAll(nonOptional.keys)) { "Missing open params" }
        }
        val plan = DataPlanner.compile(def)
        val ctx = UiContext(player.uuid.toString(), def.id, params, ConcurrentHashMap(), ConcurrentHashMap())
        val cache = NodeCache()
        val playerUuid = player.uuid.toString()

        launchPlayerLifecycle(playerUuid) {
            cancelEntry(open.remove(playerUuid))

            val entry = OpenMenu(def, plan, ctx, cache)
            open[playerUuid] = entry

            initializeState(entry)
            if (!refreshOnce(player, entry)) {
                open.remove(playerUuid)
                return@launchPlayerLifecycle
            }

            entry.job = startRefreshLoop(player, entry)
        }
    }

    fun refreshMenu(player: ServerPlayer) {
        launchPlayerLifecycle(player.stringUUID) {
            val menu = open[player.stringUUID]
                ?: return@launchPlayerLifecycle
            if (!refreshOnce(player, menu)) {
                open.remove(player.stringUUID)
                cancelEntry(menu)
            }
        }
    }

    fun closeMenu(player: ServerPlayer, withSyncId: Int? = null) {
        launchPlayerLifecycle(player.stringUUID) {
            val menu = open[player.stringUUID]
                ?: return@launchPlayerLifecycle
            if (menu.syncTracker.shouldClose(withSyncId)) {
                open.remove(player.stringUUID)
                cancelEntry(menu)
            }
        }
    }

    fun clickButton(player: ServerPlayer, menuButton: MenuButton) {
        launchPlayerLifecycle(player.stringUUID) {
            val menu = open[player.stringUUID]
                ?: return@launchPlayerLifecycle
            onMainThread {
                CommandBus.run(menuButton.cmd, menu.ctx, null, menuButton.args)
            }
        }
    }

    fun getContext(player: ServerPlayer): UiContext? {
        return open[player.uuid.toString()]?.ctx
    }

    private suspend fun initializeState(menu: OpenMenu) {
        val scope = menu.ctx.toScope()
        for ((k, v) in menu.plan.def.state) {
            val value = JsonTemplater.templatizeString(v, engine, scope)
            if (value != null) menu.ctx.state[k] = value
            else menu.ctx.state.remove(k)
        }
    }

    private fun startRefreshLoop(player: ServerPlayer, menu: OpenMenu): Job? {
        val interval = menu.def.refresh?.intervalMs ?: 0L
        if (interval <= 0) return null
        return scope.launch {
            while (isActive) {
                delay(interval)
                val shouldContinue = withPlayerLock(player.stringUUID) {
                    val activeMenu = open[player.stringUUID]
                    if (activeMenu !== menu) {
                        return@withPlayerLock false
                    }
                    val refreshed = refreshOnce(player, menu, nodes = menu.def.refresh?.nodes)
                    if (!refreshed) {
                        open.remove(player.stringUUID)
                    }
                    refreshed
                }
                if (!shouldContinue) break
            }
        }
    }

    private suspend fun cancelEntry(menu: OpenMenu?) {
        val job = menu?.job ?: return
        if (job != currentCoroutineContext()[Job]) {
            job.cancelAndJoin()
        } else {
            job.cancel()
        }
    }

    private suspend fun refreshOnce(
        player: ServerPlayer,
        menu: OpenMenu,
        nodes: List<String>? = null
    ): Boolean = onMainThread {
        // TODO: if nodes == null -> full execution; else reuse previous values and recompute only what's necessary
        executor.executeAll(menu.plan, menu.ctx, menu.cache)
        val scope = menu.ctx.toScope()
        val renderedTitle = engine.renderTemplate(menu.plan.def.title, scope)
        val rendered = renderer.renderAll(menu.plan.def, renderedTitle, menu.ctx, player)

        val openMenu =
            (player.containerMenu as? GenericInventoryScreenHandler)?.container as? InventoryMenuContainer ?: run {
                if (nodes != null) {
                    VesselMod.LOGGER.warn(
                        "Vessel UI refresh lost active menu for player='{}' menu='{}' expected='{}' actual='{}' containerId={} carried={}",
                        player.scoreboardName,
                        menu.ctx.menuId,
                        GenericInventoryScreenHandler::class.simpleName,
                        player.containerMenu::class.simpleName ?: player.containerMenu::class.java.name,
                        player.containerMenu.containerId,
                        describeStack(player.containerMenu.carried)
                    )
                }
                if (nodes == null) {
                    if (!player.containerMenu.carried.isEmpty) {
                        VesselMod.LOGGER.warn(
                            "Opening Vessel UI with non-empty carried stack for player='{}' menu='{}' actual='{}' containerId={} carried={}",
                            player.scoreboardName,
                            menu.ctx.menuId,
                            player.containerMenu::class.simpleName ?: player.containerMenu::class.java.name,
                            player.containerMenu.containerId,
                            describeStack(player.containerMenu.carried)
                        )
                    }
                    val initialMenu = InventoryMenuContainer(renderedTitle, menu.plan.def.rows, rendered.items.toMutableMap())
                    val syncId = initialMenu.open(player) ?: return@onMainThread false
                    menu.syncTracker.replaceWith(syncId)
                    return@onMainThread true
                } else {
                    return@onMainThread false
                }
            }

        if (openMenu.name == renderedTitle && openMenu.size == menu.plan.def.rows * 9) {
            openMenu.patchItems(rendered.items)
            player.containerMenu.broadcastChanges()
            true
        } else {
            val newMenu = InventoryMenuContainer(renderedTitle, menu.plan.def.rows, rendered.items.toMutableMap())
            val syncId = newMenu.open(player) ?: return@onMainThread false
            menu.syncTracker.replaceWith(syncId)
            true
        }
    }

    private fun describeStack(stack: ItemStack): String {
        if (stack.isEmpty) return "empty"
        val name = stack.itemHolder.unwrapKey().map { it.location().toString() }.orElseGet { stack.item.toString() }
        val customName = stack.get(DataComponents.CUSTOM_NAME)?.string
        return buildString {
            append(name)
            append(" x")
            append(stack.count)
            customName?.takeIf { it.isNotBlank() }?.let {
                append(" name='")
                append(it)
                append('\'')
            }
        }
    }
}
