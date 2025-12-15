package com.doofcraft.vessel.common.api.event

import com.doofcraft.vessel.common.reactive.Cancelable
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ClickAction
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

data class ItemSlotClickedEvent(
    val player: Player,
    val slot: Slot,
    val action: ClickAction,
    val stack: ItemStack,
    val otherStack: ItemStack,
) : Cancelable() {
    var result = InteractionResult.PASS
        set(value) {
            field = value
            if (value.consumesAction()) cancel()
        }
}
