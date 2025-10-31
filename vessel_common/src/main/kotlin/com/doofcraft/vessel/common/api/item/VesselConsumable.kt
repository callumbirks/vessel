package com.doofcraft.vessel.common.api.item

import com.doofcraft.vessel.common.component.ConsumableComponent
import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.ModComponents
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.UseAnim

abstract class VesselConsumable(tag: VesselTag, animation: UseAnim, duration: Int = 32): VesselItem(tag) {
    init {
        addComponent(ModComponents.CONSUMABLE) { ConsumableComponent(animation, duration) }
    }

    // `use` cannot be used on Consumables, because we will defer to `VesselBaseConsumable.use`.
    // Extra behaviours should be added to `finishUsing`.
    final override fun use(
        stack: ItemStack,
        level: ServerLevel,
        player: ServerPlayer,
        hand: InteractionHand
    ): InteractionResultHolder<ItemStack> {
        return InteractionResultHolder.pass(stack)
    }
}