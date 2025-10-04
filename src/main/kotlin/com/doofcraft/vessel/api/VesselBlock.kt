package com.doofcraft.vessel.api

import com.doofcraft.vessel.base.VesselBaseBlockEntity
import com.doofcraft.vessel.component.VesselTag
import com.doofcraft.vessel.registry.ModItems
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.LevelAccessor

abstract class VesselBlock(tag: VesselTag) : Vessel(tag) {
    override val baseItem: ItemLike = ModItems.VESSEL_BLOCK

    open fun use(
        level: ServerLevel, entity: VesselBaseBlockEntity, player: ServerPlayer, hand: InteractionHand
    ): InteractionResult {
        return InteractionResult.PASS
    }

    open fun onPlaced(level: ServerLevel, pos: BlockPos, placer: LivingEntity?, entity: VesselBaseBlockEntity) {
    }

    open fun onDestroyed(level: LevelAccessor, pos: BlockPos, entity: VesselBaseBlockEntity) {
    }
}