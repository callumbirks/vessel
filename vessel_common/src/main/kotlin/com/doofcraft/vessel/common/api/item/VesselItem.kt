package com.doofcraft.vessel.common.api.item

import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.ModItems
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

abstract class VesselItem(tag: VesselTag): Vessel(tag) {
    override val baseItem = ModItems.VESSEL

    open fun use(
        stack: ItemStack, level: ServerLevel, player: ServerPlayer, hand: InteractionHand
    ): InteractionResultHolder<ItemStack> {
        return InteractionResultHolder.pass(stack)
    }

    open fun useOnEntity(stack: ItemStack, player: ServerPlayer, entity: LivingEntity, hand: InteractionHand): InteractionResult {
        return InteractionResult.PASS
    }

    open fun onClicked(stack: ItemStack, otherStack: ItemStack, player: ServerPlayer): InteractionResult {
        return InteractionResult.PASS
    }

    // Returning anything other than 'PASS' will prevent the default Minecraft eating behaviour
    // (play sound effect, apply effects, decrement stack)
    open fun finishUsing(stack: ItemStack, level: ServerLevel, user: LivingEntity): InteractionResultHolder<ItemStack> {
        return InteractionResultHolder.pass(ItemStack.EMPTY)
    }
}