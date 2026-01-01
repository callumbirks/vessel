package com.doofcraft.vessel.common.component

import com.mojang.serialization.Codec
import net.minecraft.resources.ResourceLocation

data class ArmorRenderTextures(val layer1: ResourceLocation, val layer2: ResourceLocation) {
    companion object {
        val CODEC: Codec<ArmorRenderTextures> =
            Codec.list(ResourceLocation.CODEC, 2, 2)
                .xmap({ ArmorRenderTextures(it[0]!!, it[1]!!) }, { listOf(it.layer1, it.layer2) })
    }
}
