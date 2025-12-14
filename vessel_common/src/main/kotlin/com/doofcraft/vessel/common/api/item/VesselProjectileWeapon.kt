package com.doofcraft.vessel.common.api.item

import com.doofcraft.vessel.common.component.AnimatedUseComponent
import com.doofcraft.vessel.common.component.ProjectileWeaponData
import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.BehaviourComponents
import com.doofcraft.vessel.common.registry.ModItems
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.UseAnim

abstract class VesselProjectileWeapon(tag: VesselTag, behaviour: ProjectileWeaponData) : Vessel(tag) {
    override val baseItem = ModItems.PROJECTILE_WEAPON

    init {
        addBehaviour(BehaviourComponents.PROJECTILE_WEAPON) { behaviour }
        addBehaviour(BehaviourComponents.ANIMATED_USE) {
            AnimatedUseComponent(
                UseAnim.SPEAR, if (behaviour.chargesUp) 72000 else 0
            )
        }
    }

    /**
     * Called after the weapon has been fired.
     */
    open fun finishUsing(stack: ItemStack, level: ServerLevel, user: LivingEntity): InteractionResultHolder<ItemStack> {
        return InteractionResultHolder.pass(stack)
    }
}