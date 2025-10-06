package com.doofcraft.vessel.api.events.world

import com.doofcraft.vessel.base.VesselBaseBlockEntity
import net.minecraft.server.level.ServerLevel

data class BlockEntityUnloadEvent(
    val blockEntity: VesselBaseBlockEntity,
    val level: ServerLevel
)
