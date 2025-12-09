package com.doofcraft.vessel.common.base

import com.doofcraft.vessel.common.api.VesselBehaviourRegistry
import com.doofcraft.vessel.common.component.ProjectileData
import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.BehaviourComponents
import com.doofcraft.vessel.common.util.vesselTag
import net.minecraft.core.Direction
import net.minecraft.core.Position
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ProjectileItem
import net.minecraft.world.level.Level

open class VesselBaseProjectileItem : VesselBaseItem(), ProjectileItem {
    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val itemStack = player.getItemInHand(usedHand)
        val data =
            itemStack.vesselTag()?.let { tag -> VesselBehaviourRegistry.get(tag.key, BehaviourComponents.PROJECTILE) }
                ?: return InteractionResultHolder.fail(itemStack)

        if (!data.throwable) return InteractionResultHolder.pass(itemStack)

        data.throwSound?.let { throwSound ->
            level.playSound(
                null,
                player.x,
                player.y,
                player.z,
                throwSound,
                SoundSource.NEUTRAL,
                0.5f,
                0.4f / (level.random.nextFloat() * 0.4f + 0.8f)
            )
        }

        if (!level.isClientSide) {
            val projectile = VesselBaseProjectile(data, level, player)
            projectile.item = itemStack
            projectile.shootFromRotation(player, player.xRot, player.yRot, 0.0f, data.throwVelocity, 1.0f)
            level.addFreshEntity(projectile)
        }

        itemStack.consume(1, player)
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide)
    }

    override fun asProjectile(
        level: Level, pos: Position, stack: ItemStack, direction: Direction
    ): Projectile {
        val tag = stack.get(VesselTag.COMPONENT)
        val data =
            tag?.let { tag -> VesselBehaviourRegistry.get(tag.key, BehaviourComponents.PROJECTILE) } ?: ProjectileData(
                throwable = true, throwVelocity = 1.5f, damage = 0f, throwSound = SoundEvents.SNOWBALL_THROW
            )
        val projectile = VesselBaseProjectile(data, level, pos.x(), pos.y(), pos.z())
        projectile.item = stack
        return projectile
    }
}