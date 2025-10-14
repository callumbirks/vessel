package com.doofcraft.vessel.common.component

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.ExtraCodecs

data class MenuButton(val cmd: String, val args: Map<String, Any?> = emptyMap()) {
    companion object {
        val CODEC: Codec<MenuButton> = RecordCodecBuilder.create {
            it.group(
                Codec.STRING.fieldOf("cmd").forGetter(MenuButton::cmd),
                Codec.unboundedMap<String, Any?>(Codec.STRING, ExtraCodecs.JAVA)
                    .fieldOf("data")
                    .forGetter(MenuButton::args)
            ).apply(it, ::MenuButton)
        }
    }
}
