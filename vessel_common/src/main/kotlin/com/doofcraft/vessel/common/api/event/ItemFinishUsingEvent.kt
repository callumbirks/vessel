package com.doofcraft.vessel.common.api.event

import com.doofcraft.vessel.common.reactive.Cancelable
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

data class ItemFinishUsingEvent(
    val level: Level, val user: LivingEntity, val stack: ItemStack
) : Cancelable() {
    var result: InteractionResultHolder<ItemStack> = InteractionResultHolder.pass(stack)
        set(value) {
            field = value
            if (value.result.consumesAction()) cancel()
        }
}
