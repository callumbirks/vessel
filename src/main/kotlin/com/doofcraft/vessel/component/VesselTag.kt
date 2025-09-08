package com.doofcraft.vessel.component

import com.doofcraft.vessel.registry.ModComponents
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.component.ComponentType

data class VesselTag(val key: String, val type: String) {
    companion object {
        val CODEC: Codec<VesselTag> = RecordCodecBuilder.create {
            it.group(
                Codec.STRING.fieldOf("key").forGetter(VesselTag::key),
                Codec.STRING.fieldOf("type").forGetter(VesselTag::type)
            ).apply(it, ::VesselTag)
        }

        val COMPONENT: ComponentType<VesselTag>
            get() = ModComponents.VESSEL_TAG
    }
}
