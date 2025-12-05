package com.doofcraft.vessel.server.ui.handler

import com.doofcraft.vessel.common.registry.StackComponents
import com.doofcraft.vessel.server.ui.UiManager
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class GenericInventoryScreenHandler(syncId: Int, private val playerInventory: Inventory, inventory: Container) : ChestMenu(
    genericHandler(inventory.containerSize),
    syncId,
    playerInventory,
    inventory,
    inventory.containerSize / 9,
) {
    override fun quickMoveStack(player: Player, index: Int): ItemStack? {
        return ItemStack.EMPTY
    }

    override fun moveItemStackTo(stack: ItemStack, startIndex: Int, endIndex: Int, reverseDirection: Boolean): Boolean {
        if (startIndex < container.containerSize) {
            // Prevent inserting items into the inventory slots
            return false
        }
        return super.moveItemStackTo(stack, startIndex, endIndex, reverseDirection)
    }

    // Only allow inserting items into the player's own inventory
    override fun canDragTo(slot: Slot): Boolean {
        return slot.container == playerInventory && super.canDragTo(slot)
    }

    override fun setItem(slotId: Int, stateId: Int, stack: ItemStack) {
        if (slotId < container.containerSize) {
            // Prevent settings items in the inventory slots
            return
        }
        return super.setItem(slotId, stateId, stack)
    }

    override fun clicked(slotId: Int, button: Int, clickType: ClickType, player: Player) {
        // Only allow interaction outside the inventory slots
        if (slotId >= 0 && slotId < container.containerSize) {
            // A pickup action could also be the player dropping items from their cursor into the inventory,
            // if they do this we need to return the item to the player's inventory
            if (clickType == ClickType.PICKUP) {
                if (!carried.isEmpty) {
                    // If the cursor stack is not empty, make sure it doesn't change.
                    setRemoteCarried(carried)
                    return
                }
                // If the cursor stack is empty, this could be a button press.
                val stack = container.getItem(slotId)
                val buttonData = stack[StackComponents.MENU_BUTTON] ?: return
                UiManager.service.clickButton(player as ServerPlayer, buttonData)
            }
        }
        super.clicked(slotId, button, clickType, player)
    }

    companion object {
        private fun genericHandler(size: Int): MenuType<ChestMenu> {
            return when (size) {
                9 -> MenuType.GENERIC_9x1
                18 -> MenuType.GENERIC_9x2
                27 -> MenuType.GENERIC_9x3
                36 -> MenuType.GENERIC_9x4
                45 -> MenuType.GENERIC_9x5
                54 -> MenuType.GENERIC_9x6
                else -> {
                    throw IllegalArgumentException("Unsupported InventoryMenu size: $size")
                }
            }
        }
    }
}
