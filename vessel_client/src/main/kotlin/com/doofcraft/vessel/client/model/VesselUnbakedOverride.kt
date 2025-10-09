package com.doofcraft.vessel.client.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.resources.ResourceLocation

@Serializable
data class VesselUnbakedOverride(
    @SerialName("when") val predicate: VesselPredicate,
    @Contextual val model: ResourceLocation
)
