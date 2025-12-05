package com.doofcraft.vessel.client.model

import com.doofcraft.vessel.common.predicate.VesselPredicate
import net.minecraft.client.resources.model.BakedModel

data class VesselBakedOverride(
    val predicate: VesselPredicate,
    val model: BakedModel
)
