package com.doofcraft.vessel.common.component

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.ExtraCodecs

data class MenuButton(val cmd: String, val args: Map<String, Any> = emptyMap()) {
    companion object {
        val CODEC: Codec<MenuButton> = RecordCodecBuilder.create {
            it.group(
                Codec.STRING.fieldOf("cmd").forGetter(MenuButton::cmd),
                Codec.unboundedMap<String, Any?>(Codec.STRING, ExtraCodecs.JAVA)
                    .fieldOf("data")
                    .forGetter(MenuButton::args)
            ).apply(it, ::MenuButton)
        }

        val NOOP_CODEC: StreamCodec<ByteBuf, MenuButton> = object : StreamCodec<ByteBuf, MenuButton> {
            override fun encode(`object`: ByteBuf, object2: MenuButton) { /* no-op */ }
            override fun decode(`object`: ByteBuf) = EMPTY
        }

        val EMPTY = MenuButton("", emptyMap())
    }
}
