package com.doofcraft.vessel.common.component

import com.doofcraft.vessel.common.predicate.VesselPredicate
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.ItemStack

data class ProjectileWeaponComponent(
    val chargesUp: Boolean,
    val requiresAmmo: Boolean,
    val ammoPredicate: VesselPredicate,
    val defaultAmmo: ItemStack,
    val shootSound: Holder<SoundEvent> = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ARROW_SHOOT)
) {
    constructor(
        chargesUp: Boolean,
        requiresAmmo: Boolean,
        ammoPredicate: VesselPredicate,
        defaultAmmo: ItemStack,
        shootSound: SoundEvent
    ) : this(
        chargesUp, requiresAmmo, ammoPredicate, defaultAmmo, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(shootSound)
    )

    companion object {
        val CODEC: Codec<ProjectileWeaponComponent> = RecordCodecBuilder.create {
            it.group(
                Codec.BOOL.fieldOf("charges").forGetter(ProjectileWeaponComponent::chargesUp),
                Codec.BOOL.fieldOf("ammo_req").forGetter(ProjectileWeaponComponent::requiresAmmo),
                VesselPredicate.CODEC.fieldOf("ammo_pred").forGetter(ProjectileWeaponComponent::ammoPredicate),
                ItemStack.SINGLE_ITEM_CODEC.fieldOf("ammo_default").forGetter(ProjectileWeaponComponent::defaultAmmo),
                SoundEvent.CODEC.fieldOf("sound").forGetter(ProjectileWeaponComponent::shootSound)
            ).apply(it, ::ProjectileWeaponComponent)
        }
    }
}
