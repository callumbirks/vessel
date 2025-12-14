package com.doofcraft.vessel.common.api.event

import com.doofcraft.vessel.common.base.VesselBaseProjectile
import net.minecraft.world.phys.EntityHitResult

data class ProjectileHitEntityEvent(
    val hitResult: EntityHitResult,
    val projectile: VesselBaseProjectile,
)
