package com.doofcraft.vessel.api

import com.doofcraft.vessel.base.VesselBaseBlockEntity
import com.doofcraft.vessel.component.VesselTag
import com.doofcraft.vessel.registry.ModItems
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemActionResult

abstract class VesselBlock(tag: VesselTag) : Vessel(tag) {
    override val baseItem = ModItems.VESSEL_BLOCK

    open fun use(
        world: ServerWorld, entity: VesselBaseBlockEntity, player: ServerPlayerEntity, hand: Hand
    ): ActionResult {
        return ActionResult.PASS
    }

    open fun useWithItem(
        world: ServerWorld, entity: VesselBaseBlockEntity, player: ServerPlayerEntity, stack: ItemStack, hand: Hand
    ): ItemActionResult {
        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
    }
}