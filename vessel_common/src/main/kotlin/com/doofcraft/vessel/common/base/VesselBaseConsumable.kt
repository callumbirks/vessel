package com.doofcraft.vessel.common.base

import com.doofcraft.vessel.common.api.VesselBehaviourRegistry
import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.BehaviourComponents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemUtils
import net.minecraft.world.level.Level

open class VesselBaseConsumable() : VesselBaseItem() {
    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(usedHand)
        val tag = stack.get(VesselTag.COMPONENT) ?: return super.use(level, player, usedHand)
        return if (VesselBehaviourRegistry.has(tag.key, BehaviourComponents.ANIMATED_USE)) {
            ItemUtils.startUsingInstantly(level, player, usedHand)
        } else {
            super.use(level, player, usedHand)
        }
    }
}