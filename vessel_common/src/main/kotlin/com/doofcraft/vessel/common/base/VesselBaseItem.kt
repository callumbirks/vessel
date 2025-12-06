package com.doofcraft.vessel.common.base

import com.doofcraft.vessel.common.api.VesselBehaviourRegistry
import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.BehaviourComponents
import com.doofcraft.vessel.common.util.ItemHelpers
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemUtils
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level

open class VesselBaseItem() : Item(Properties()) {
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