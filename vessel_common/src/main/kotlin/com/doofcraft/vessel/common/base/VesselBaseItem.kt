package com.doofcraft.vessel.common.base

import com.doofcraft.vessel.common.util.ItemHelpers
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim

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
}