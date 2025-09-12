package com.doofcraft.vessel.api

import com.doofcraft.vessel.base.VesselBaseBlockEntity
import com.doofcraft.vessel.component.VesselTag
import com.doofcraft.vessel.registry.ModItems
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand

abstract class VesselBlock(tag: VesselTag): Vessel(tag) {
    override val baseItem = ModItems.VESSEL_BLOCK

    open fun use(world: ServerWorld, entity: VesselBaseBlockEntity, player: ServerPlayerEntity, hand: Hand): ActionResult {
        return ActionResult.PASS
    }
}