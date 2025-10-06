package com.doofcraft.vessel.component

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.StringRepresentable

data class MenuButton(val action: Action, val data: Map<String, String> = emptyMap()) {
    enum class Action : StringRepresentable {
        NAVIGATE, CLOSE, ACCEPT;

        override fun getSerializedName() = this.name

        companion object {
            @JvmStatic
            val CODEC: Codec<Action> = StringRepresentable.fromEnum(Action::values)
        }
    }

    companion object {
        val CODEC: Codec<MenuButton> = RecordCodecBuilder.create {
            it.group(
                Action.CODEC.fieldOf("action").forGetter(MenuButton::action),
                Codec.unboundedMap<String, String>(Codec.STRING, Codec.STRING)
                    .fieldOf("data")
                    .forGetter(MenuButton::data)
            ).apply(it, ::MenuButton)
        }
    }
}
