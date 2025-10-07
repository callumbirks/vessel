package com.doofcraft.vessel.common.util

import net.minecraft.resources.ResourceLocation

fun vesselResource(name: String): ResourceLocation {
    return ResourceLocation.fromNamespaceAndPath(VesselMod.MODID, name)
}

