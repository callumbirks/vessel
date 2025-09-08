package com.doofcraft.vessel.api

import com.doofcraft.vessel.component.VesselTag
import com.doofcraft.vessel.registry.ModItems
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult

abstract class VesselItem(tag: VesselTag): Vessel(tag) {
    override val baseItem = ModItems.VESSEL


    open fun use(
        stack: ItemStack, world: ServerWorld, player: ServerPlayerEntity, hand: Hand
    ): TypedActionResult<ItemStack> {
        return TypedActionResult.pass(stack)
    }

    open fun useOnEntity(stack: ItemStack, player: ServerPlayerEntity, entity: LivingEntity, hand: Hand): ActionResult {
        return ActionResult.PASS
    }

    open fun onClicked(stack: ItemStack, otherStack: ItemStack, player: ServerPlayerEntity): ActionResult {
        return ActionResult.PASS
    }

    // Returning anything other than 'PASS' will prevent the default Minecraft eating behaviour
    // (play sound effect, apply effects, decrement stack)
    open fun finishUsing(stack: ItemStack, world: ServerWorld, user: LivingEntity): TypedActionResult<ItemStack> {
        return TypedActionResult.pass(ItemStack.EMPTY)
    }
}