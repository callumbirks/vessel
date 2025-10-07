package com.doofcraft.vessel.server.util

import net.minecraft.resources.ResourceLocation

fun ResourceLocation.endsWith(suffix: String): Boolean {
    return this.toString().endsWith(suffix)
}