package com.doofcraft.vessel.common.api.event

import com.doofcraft.vessel.common.base.VesselBaseBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

data class BlockDestroyedEvent(
    val level: Level,
    val pos: BlockPos,
    val blockEntity: VesselBaseBlockEntity,
)