package com.doofcraft.vessel.common.api.event

import com.doofcraft.vessel.common.reactive.Cancelable
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

data class ItemUseOnEntityEvent(
    val level: Level,
    val player: Player,
    val target: LivingEntity,
    val hand: InteractionHand,
    val stack: ItemStack,
) : Cancelable() {
    var result = InteractionResult.PASS
        set(value) {
            field = value
            if (value.consumesAction()) cancel()
        }
}
