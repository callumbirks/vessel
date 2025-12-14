package com.doofcraft.vessel.common.api.event

import com.doofcraft.vessel.common.reactive.Cancelable
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

data class ItemUseEvent(
    val level: Level,
    val player: Player,
    val hand: InteractionHand,
    val stack: ItemStack,
) : Cancelable() {
    var result: InteractionResultHolder<ItemStack> = InteractionResultHolder.pass(stack)
        set(value) {
            field = value
            if (value.result.consumesAction()) cancel()
        }
}
