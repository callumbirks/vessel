package com.doofcraft.vessel.model

import net.minecraft.resources.ResourceLocation

data class VesselUnbakedOverride(
    val predicate: VesselPredicate,
    val model: ResourceLocation
)
