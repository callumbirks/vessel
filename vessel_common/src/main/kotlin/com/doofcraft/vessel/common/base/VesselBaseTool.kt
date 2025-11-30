package com.doofcraft.vessel.common.base

import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.ModComponents
import com.doofcraft.vessel.common.tooltip.TooltipRegistry
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

open class VesselBaseTool: Item(Properties()) {
    override fun getDescriptionId(stack: ItemStack): String? {
        val tag = stack.get(VesselTag.COMPONENT) ?: return super.getDescriptionId()
        return "item.${tag.key}"
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        val tag = stack.get(VesselTag.COMPONENT) ?: return
        val tooltips = TooltipRegistry.getTooltip(tag.key) ?: return
        tooltipComponents.addAll(tooltips)
    }

    // Same as diamond tier
    override fun getEnchantmentValue(): Int = 10

    // Copy some behaviours from DiggerItem / TieredItem
    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        return true
    }

    // Same as TieredItem but using components.
    override fun isValidRepairItem(stack: ItemStack, repairCandidate: ItemStack): Boolean {
        val data = stack.get(ModComponents.INGREDIENT) ?: return false
        return data.ingredient.test(repairCandidate)
    }
}