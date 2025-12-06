package com.doofcraft.vessel.common.util

import com.doofcraft.vessel.common.base.VesselBaseProjectileWeapon
import com.doofcraft.vessel.common.component.ProjectileWeaponData
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ProjectileWeaponItem
import java.util.function.Predicate

// Modified from Player::getProjectile to fit VesselBaseProjectileWeapon
fun Player.getVesselProjectile(weaponStack: ItemStack, config: ProjectileWeaponData): ItemStack {
    if (weaponStack.item !is VesselBaseProjectileWeapon) {
        return ItemStack.EMPTY
    }

    val predicate: Predicate<ItemStack> = Predicate { config.ammoPredicate.test(it) }
    // Shortcut to test held items
    val itemStack = ProjectileWeaponItem.getHeldProjectile(this, predicate)
    if (!itemStack.isEmpty) {
        return itemStack
    }
    // Search the whole inventory
    for (stack in this.inventory.items) {
        if (predicate.test(stack)) {
            return stack
        }
    }

    return if (!config.requiresAmmo || abilities.instabuild) config.defaultAmmo.copy() else ItemStack.EMPTY
}