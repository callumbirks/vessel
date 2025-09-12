package com.doofcraft.vessel.model

import net.minecraft.util.Identifier

data class VesselUnbakedOverride(
    val predicate: VesselPredicate,
    val model: Identifier
)
