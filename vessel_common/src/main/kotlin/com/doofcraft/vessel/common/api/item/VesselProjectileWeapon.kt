package com.doofcraft.vessel.common.api.item

import com.doofcraft.vessel.common.api.VesselBehaviourRegistry
import com.doofcraft.vessel.common.component.ProjectileWeaponComponent
import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.BehaviourComponents
import com.doofcraft.vessel.common.registry.ModItems

abstract class VesselProjectileWeapon(
    tag: VesselTag, behaviour: ProjectileWeaponComponent
) : Vessel(tag) {
    override val baseItem = ModItems.PROJECTILE_WEAPON

    init {
        VesselBehaviourRegistry.set(tag.key, BehaviourComponents.PROJECTILE_WEAPON, behaviour)
    }
}