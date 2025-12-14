package com.doofcraft.vessel.common.api.event

import com.doofcraft.vessel.common.base.VesselBaseBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level

data class BlockPlacedEvent(
    val level: Level,
    val pos: BlockPos,
    val placer: LivingEntity?,
    val blockEntity: VesselBaseBlockEntity,
)
