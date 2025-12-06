package com.doofcraft.vessel.common.network

import com.doofcraft.vessel.common.util.vesselResource
import net.minecraft.core.component.DataComponentMap
import net.minecraft.network.RegistryFriendlyByteBuf

class SetBehaviourS2CPacket(val key: String, val components: DataComponentMap) : NetworkPacket<SetBehaviourS2CPacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUtf(key)
        buffer.writeJsonWithCodec(DataComponentMap.CODEC, components)
    }

    companion object {
        val ID = vesselResource("packet/s2c/set_behaviour")

        fun decode(buffer: RegistryFriendlyByteBuf): SetBehaviourS2CPacket {
            val key = buffer.readUtf()
            val components = buffer.readJsonWithCodec(DataComponentMap.CODEC)
            return SetBehaviourS2CPacket(key, components)
        }
    }
}