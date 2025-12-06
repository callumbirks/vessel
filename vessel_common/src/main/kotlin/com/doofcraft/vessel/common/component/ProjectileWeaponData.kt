package com.doofcraft.vessel.common.component

import com.doofcraft.vessel.common.predicate.VesselPredicate
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.ItemStack

data class ProjectileWeaponData(
    val chargesUp: Boolean,
    val requiresAmmo: Boolean,
    val ammoPredicate: VesselPredicate,
    val defaultAmmo: ItemStack,
    val shootSound: Holder<SoundEvent>,
    val maxVelocity: Float,
    val cooldown: Int,
) {
    constructor(
        chargesUp: Boolean,
        requiresAmmo: Boolean,
        ammoPredicate: VesselPredicate,
        defaultAmmo: ItemStack,
        shootSound: SoundEvent,
        maxVelocity: Float,
        cooldown: Int
    ) : this(
        chargesUp,
        requiresAmmo,
        ammoPredicate,
        defaultAmmo,
        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(shootSound),
        maxVelocity,
        cooldown
    )

    companion object {
        val CODEC: Codec<ProjectileWeaponData> = RecordCodecBuilder.create {
            it.group(
                Codec.BOOL.fieldOf("charges").forGetter(ProjectileWeaponData::chargesUp),
                Codec.BOOL.fieldOf("ammo_req").forGetter(ProjectileWeaponData::requiresAmmo),
                VesselPredicate.CODEC.fieldOf("ammo_pred").forGetter(ProjectileWeaponData::ammoPredicate),
                ItemStack.SINGLE_ITEM_CODEC.fieldOf("ammo_default").forGetter(ProjectileWeaponData::defaultAmmo),
                SoundEvent.CODEC.fieldOf("sound").forGetter(ProjectileWeaponData::shootSound),
                Codec.FLOAT.fieldOf("vmax").forGetter(ProjectileWeaponData::maxVelocity),
                Codec.INT.fieldOf("cooldown").forGetter(ProjectileWeaponData::cooldown)
            ).apply(it, ::ProjectileWeaponData)
        }
    }
}
