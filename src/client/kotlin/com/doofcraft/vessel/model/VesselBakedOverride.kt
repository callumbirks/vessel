package com.doofcraft.vessel.model

import net.minecraft.client.render.model.BakedModel

data class VesselBakedOverride(
    val predicate: VesselPredicate,
    val model: BakedModel
)
