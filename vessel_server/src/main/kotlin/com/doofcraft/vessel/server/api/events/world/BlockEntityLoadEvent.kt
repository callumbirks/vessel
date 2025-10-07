package com.doofcraft.vessel.server.api.events.world

import com.doofcraft.vessel.common.base.VesselBaseBlockEntity
import net.minecraft.server.level.ServerLevel

data class BlockEntityLoadEvent(
    val blockEntity: VesselBaseBlockEntity,
    val level: ServerLevel
)
