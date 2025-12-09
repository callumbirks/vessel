package com.doofcraft.vessel.common.api.item

import com.doofcraft.vessel.common.component.ProjectileData
import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.BehaviourComponents
import com.doofcraft.vessel.common.registry.ModItems
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.ItemStack

abstract class VesselProjectile(tag: VesselTag, data: ProjectileData) : VesselItem(tag) {
    init {
        addBehaviour(BehaviourComponents.PROJECTILE) { data }
    }

    override val baseItem = ModItems.PROJECTILE_ITEM

    // Use will always throw a throwable projectile, so disallow
    override fun use(
        stack: ItemStack,
        level: ServerLevel,
        player: ServerPlayer,
        hand: InteractionHand
    ): InteractionResultHolder<ItemStack> {
        return InteractionResultHolder.pass(stack)
    }
}