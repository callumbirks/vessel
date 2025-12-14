package com.doofcraft.vessel.server.ui.handler

import com.doofcraft.vessel.server.api.events.VesselServerEvents
import com.doofcraft.vessel.server.api.events.ui.ContainerMenuClosedEvent
import com.doofcraft.vessel.server.api.events.ui.ContainerMenuOpenedEvent
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack

class InventoryMenuContainer(val name: String, rows: Int, private val items: MutableMap<Int, ItemStack>) : Container {
    var syncId: Int = -1
        private set
    val size = rows * 9

    fun patchItems(next: Map<Int, ItemStack>) {
        fun equalStacks(a: ItemStack?, b: ItemStack?): Boolean {
            if (a == null || b == null) return a == b
            return ItemStack.isSameItemSameComponents(a, b)
        }

        for (k in items.keys.union(next.keys)) {
            val nextStack = next[k]
            if (!equalStacks(items[k], nextStack)) {
                if (nextStack == null) items.remove(k)
                else items[k] = nextStack
            }
        }
    }

    /** Do **NOT** use directly! Use `InventoryMenuManager.openMenu(player, menu)` instead. */
    fun open(player: ServerPlayer): Boolean {
        val factory = object : MenuProvider {
            override fun getDisplayName(): Component = Component.literal(name)

            override fun createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu? {
                return GenericInventoryScreenHandler(i, inventory, this@InventoryMenuContainer)
            }

            override fun shouldCloseCurrentScreen(): Boolean = true
        }
        syncId = player.openMenu(factory).orElse(-1)
        return if (syncId != -1) {
            VesselServerEvents.CONTAINER_MENU_OPENED.emit(ContainerMenuOpenedEvent(player, syncId))
            true
        } else {
            false
        }
    }

    override fun getContainerSize() = this.size

    override fun isEmpty(): Boolean = items.isEmpty()

    override fun getItem(slot: Int): ItemStack = items[slot] ?: ItemStack.EMPTY // ?: menu.decorator()

    override fun removeItem(slot: Int, amount: Int): ItemStack? {
        return ItemStack.EMPTY
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack? {
        return ItemStack.EMPTY
    }

    override fun setItem(slot: Int, stack: ItemStack) {}

    override fun setChanged() {}

    override fun stillValid(player: Player): Boolean = true

    override fun clearContent() {}

    override fun stopOpen(player: Player) {
        VesselServerEvents.CONTAINER_MENU_CLOSED.emit(ContainerMenuClosedEvent(player as ServerPlayer, syncId))
    }
}
