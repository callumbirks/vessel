package com.doofcraft.vessel.common.util

import com.doofcraft.vessel.common.VesselMod
import net.minecraft.resources.ResourceLocation

fun vesselResource(name: String): ResourceLocation {
    return ResourceLocation.fromNamespaceAndPath(VesselMod.MODID, name)
}

