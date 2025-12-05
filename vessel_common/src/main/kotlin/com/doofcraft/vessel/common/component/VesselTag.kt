package com.doofcraft.vessel.common.component

import com.doofcraft.vessel.common.registry.StackComponents
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.component.DataComponentType

data class VesselTag(@JvmField val key: String, @JvmField val type: String) {
    companion object {
        val CODEC: Codec<VesselTag> = RecordCodecBuilder.create {
            it.group(
                Codec.STRING.fieldOf("key").forGetter(VesselTag::key),
                Codec.STRING.fieldOf("type").forGetter(VesselTag::type)
            ).apply(it, ::VesselTag)
        }

        val COMPONENT: DataComponentType<VesselTag>
            get() = StackComponents.VESSEL_TAG
    }
}
