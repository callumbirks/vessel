package com.doofcraft.vessel.common.network

import com.doofcraft.vessel.common.util.vesselResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization

class ReloadTooltipsS2CPacket(val tooltips: Map<String, List<Component>>) : NetworkPacket<ReloadTooltipsS2CPacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeMap(tooltips, { buf, k ->
            buf.writeUtf(k)
        }, { buf, v ->
            buf.writeCollection(v) { buf, e ->
                buf.writeJsonWithCodec(ComponentSerialization.CODEC, e)
            }
        })
    }

    companion object {
        val ID = vesselResource("packet/s2c/reload_tooltips")

        fun decode(buffer: RegistryFriendlyByteBuf): ReloadTooltipsS2CPacket {
            val tooltips = buffer.readMap({ buf ->
                buf.readUtf()
            }, { buf ->
                buf.readCollection({ mutableListOf<Component>() }, { buf ->
                    buf.readJsonWithCodec(ComponentSerialization.CODEC)
                })
            })
            return ReloadTooltipsS2CPacket(tooltips)
        }
    }
}