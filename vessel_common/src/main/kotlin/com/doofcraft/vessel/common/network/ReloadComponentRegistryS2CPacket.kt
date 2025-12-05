package com.doofcraft.vessel.common.network

import com.doofcraft.vessel.common.util.vesselResource
import net.minecraft.core.component.DataComponentMap
import net.minecraft.network.RegistryFriendlyByteBuf

class ReloadComponentRegistryS2CPacket(val components: Map<String, DataComponentMap>) :
    NetworkPacket<ReloadComponentRegistryS2CPacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeMap(components, { buf, k ->
            buf.writeUtf(k)
        }, { buf, v ->
            buf.writeJsonWithCodec(DataComponentMap.CODEC, v)
        })
    }

    companion object {
        val ID = vesselResource("packet/s2c/reload_components")

        fun decode(buffer: RegistryFriendlyByteBuf): ReloadComponentRegistryS2CPacket {
            val components = buffer.readMap({ buf ->
                buf.readUtf()
            }, { buf ->
                buf.readJsonWithCodec(DataComponentMap.CODEC)
            })
            return ReloadComponentRegistryS2CPacket(components)
        }
    }
}