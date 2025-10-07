package com.doofcraft.vessel.server.api

import com.mojang.serialization.Codec
import net.minecraft.util.StringRepresentable

enum class Priority : StringRepresentable {
    HIGHEST,
    HIGH,
    NORMAL,
    LOW,
    LOWEST;

    override fun getSerializedName() = this.name

    companion object {
        @JvmStatic
        val CODEC: Codec<Priority> = StringRepresentable.fromEnum(Priority::values)
    }
}
