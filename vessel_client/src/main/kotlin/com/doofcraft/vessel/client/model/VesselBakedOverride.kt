package com.doofcraft.vessel.client.model

import net.minecraft.client.resources.model.BakedModel

data class VesselBakedOverride(
    val predicate: VesselPredicate,
    val model: BakedModel
)
