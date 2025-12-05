@file:UseSerializers(ResourceLocationSerializer::class)

package com.doofcraft.vessel.client.model

import com.doofcraft.vessel.common.predicate.VesselPredicate
import com.doofcraft.vessel.common.serialization.adapters.ResourceLocationSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.resources.ResourceLocation

@Serializable
data class VesselUnbakedOverride(
    @SerialName("when") val predicate: VesselPredicate,
    val model: ResourceLocation
)
