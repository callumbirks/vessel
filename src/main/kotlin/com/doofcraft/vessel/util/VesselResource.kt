package com.doofcraft.vessel.util

import com.doofcraft.vessel.VesselMod
import net.minecraft.resources.ResourceLocation

fun vesselResource(name: String): ResourceLocation {
    return ResourceLocation.fromNamespaceAndPath(VesselMod.MODID, name)
}

