package com.doofcraft.vessel.common.component

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

data class MenuButton(val cmd: String, val args: Map<String, String> = emptyMap()) {
    companion object {
        val CODEC: Codec<MenuButton> = RecordCodecBuilder.create {
            it.group(
                Codec.STRING.fieldOf("cmd").forGetter(MenuButton::cmd),
                Codec.unboundedMap<String, String>(Codec.STRING, Codec.STRING)
                    .fieldOf("data")
                    .forGetter(MenuButton::args)
            ).apply(it, ::MenuButton)
        }
    }
}
