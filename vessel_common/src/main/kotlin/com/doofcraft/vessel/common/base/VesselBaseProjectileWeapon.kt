package com.doofcraft.vessel.common.base

import com.doofcraft.vessel.common.api.VesselBehaviourRegistry
import com.doofcraft.vessel.common.component.CooldownComponent
import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.BehaviourComponents
import com.doofcraft.vessel.common.registry.StackComponents
import com.doofcraft.vessel.common.util.ItemHelpers
import com.doofcraft.vessel.common.util.getVesselProjectile
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.AbstractArrow
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.BowItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.ProjectileItem
import net.minecraft.world.item.ProjectileWeaponItem
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.function.Predicate

open class VesselBaseProjectileWeapon : ProjectileWeaponItem(Properties()) {
    // Same as ProjectileWeaponItem
    override fun getEnchantmentValue(): Int = 1

    // Just return false - we use our own implementation to fetch projectiles
    override fun getAllSupportedProjectiles(): Predicate<ItemStack?> = Predicate { false }

    // Same as BowItem
    override fun getDefaultProjectileRange(): Int = 15

    // Similar to BowItem::releaseUsing but modified to support our custom projectiles and weapons with no ammo
    override fun releaseUsing(stack: ItemStack, level: Level, livingEntity: LivingEntity, timeCharged: Int) {
        if (livingEntity !is Player) return
        useNow(stack, level, livingEntity, timeCharged)
    }

    override fun allowComponentsUpdateAnimation(
        player: Player, hand: InteractionHand, oldStack: ItemStack, newStack: ItemStack
    ): Boolean {
        return false
    }

    fun useNow(stack: ItemStack, level: Level, player: Player, timeCharged: Int) {
        val tag = stack.get(VesselTag.COMPONENT) ?: return
        val config = VesselBehaviourRegistry.get(tag.key, BehaviourComponents.PROJECTILE_WEAPON) ?: return

        val projectileStack = player.getVesselProjectile(stack, config)
        if (projectileStack.isEmpty) {
            return
        }
        val duration = this.getUseDuration(stack, player) - timeCharged
        val power = if (config.chargesUp) BowItem.getPowerForTime(duration) else 1f
        if (power < 0.1f) {
            return
        }

        if (level is ServerLevel && config.cooldown > 0) {
            stack.set(
                StackComponents.COOLDOWN, CooldownComponent(endTick = level.server.tickCount + config.cooldown)
            )
        }

        val projectiles = draw(stack, projectileStack, player)

        if (level is ServerLevel && projectiles.isNotEmpty()) {
            shoot(
                level,
                player,
                player.usedItemHand,
                stack,
                projectiles,
                power * config.maxVelocity,
                1f,
                config.chargesUp && power == 1f,
                null
            )
        }

        level.playSound(
            null,
            player.x,
            player.y,
            player.z,
            config.shootSound,
            SoundSource.PLAYERS,
            1f,
            1f / (level.random.nextFloat() * 0.4f + 1.2f) + power * 0.5f
        )
    }

    // Similar to BowItem::use but modified to support our custom projectiles and weapons with no ammo
    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val itemStack = player.getItemInHand(usedHand)
        val tag = itemStack.get(VesselTag.COMPONENT) ?: return InteractionResultHolder.pass(itemStack)
        val config =
            VesselBehaviourRegistry.get(tag.key, BehaviourComponents.PROJECTILE_WEAPON)
                ?: return InteractionResultHolder.pass(itemStack)

        if (itemStack.has(StackComponents.COOLDOWN)) {
            return InteractionResultHolder.pass(itemStack)
        }

        val hasAmmo = player.getVesselProjectile(itemStack, config).isEmpty.not()

        if (config.requiresAmmo && !player.hasInfiniteMaterials() && !hasAmmo) {
            return InteractionResultHolder.fail(itemStack)
        } else if (config.chargesUp) {
            player.startUsingItem(usedHand)
            return InteractionResultHolder.consume(itemStack)
        } else {
            useNow(itemStack, level, player, timeCharged = 0)
            return InteractionResultHolder.consume(itemStack)
        }
    }

    // Modified from ProjectileWeaponItem::createProjectile to support projectiles other than Arrow.
    override fun createProjectile(
        level: Level, shooter: LivingEntity, weapon: ItemStack, ammo: ItemStack, isCrit: Boolean
    ): Projectile {
        val projectileItem = ammo.item as? ProjectileItem ?: Items.ARROW as ProjectileItem

        val tag = weapon.get(VesselTag.COMPONENT)
        val config = tag?.let { tag -> VesselBehaviourRegistry.get(tag.key, BehaviourComponents.PROJECTILE_WEAPON) }

        val pos = Vec3(shooter.x, shooter.eyeY - 0.1f, shooter.z)
        val projectile = projectileItem.asProjectile(level, pos, ammo.copyWithCount(1), shooter.direction)
        projectile.owner = shooter
        if (projectile is AbstractArrow) {
            // If the weapon doesn't use ammo, the ammo will be spawned in, so we don't want it to be picked up after.
            if (config?.requiresAmmo == false) {
                projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY
            }
            if (isCrit) {
                projectile.isCritArrow = true
            }
        }
        return projectile
    }

    // Copied from BowItem::shootProjectile
    override fun shootProjectile(
        shooter: LivingEntity,
        projectile: Projectile,
        index: Int,
        velocity: Float,
        inaccuracy: Float,
        angle: Float,
        target: LivingEntity?
    ) {
        projectile.shootFromRotation(shooter, shooter.xRot, shooter.yRot + angle, 0.0F, velocity, inaccuracy);
    }

    override fun getDescriptionId(stack: ItemStack): String? {
        return ItemHelpers.getDescriptionId(stack) { super.getDescriptionId(stack) }
    }

    override fun appendHoverText(
        stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component?>, tooltipFlag: TooltipFlag
    ) {
        return ItemHelpers.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
    }

    override fun getUseAnimation(stack: ItemStack): UseAnim {
        return ItemHelpers.getUseAnimation(stack) { stack -> super.getUseAnimation(stack) }
    }

    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int {
        return ItemHelpers.getUseDuration(stack, entity) { stack, entity -> super.getUseDuration(stack, entity) }
    }

    override fun inventoryTick(stack: ItemStack, level: Level, entity: Entity, slotId: Int, isSelected: Boolean) {
        if (level is ServerLevel) {
            ItemHelpers.removeExpiredCooldown(stack, level.server)
        }
    }
}