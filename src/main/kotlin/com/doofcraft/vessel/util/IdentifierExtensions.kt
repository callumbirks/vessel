package com.doofcraft.vessel.util

import net.minecraft.resources.ResourceLocation

fun ResourceLocation.endsWith(suffix: String): Boolean {
    return this.toString().endsWith(suffix)
}