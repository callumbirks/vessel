package com.doofcraft.vessel.common.component

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundEvent

data class ProjectileData(
    val throwable: Boolean, val throwVelocity: Float, val damage: Float, val throwSound: Holder<SoundEvent>?
) {
    constructor(throwable: Boolean, throwVelocity: Float, damage: Float, throwSound: SoundEvent?) : this(
        throwable, throwVelocity, damage, throwSound?.let { BuiltInRegistries.SOUND_EVENT.wrapAsHolder(it) })

    companion object {
        val CODEC: Codec<ProjectileData> = RecordCodecBuilder.create {
            it.group(
                Codec.BOOL.fieldOf("throwable").forGetter(ProjectileData::throwable),
                Codec.FLOAT.fieldOf("throw_velocity").forGetter(ProjectileData::throwVelocity),
                Codec.FLOAT.fieldOf("damage").forGetter(ProjectileData::damage),
                SoundEvent.CODEC.optionalFieldOf("throw_sound", null).forGetter(ProjectileData::throwSound),
            ).apply(it, ::ProjectileData)
        }
    }
}