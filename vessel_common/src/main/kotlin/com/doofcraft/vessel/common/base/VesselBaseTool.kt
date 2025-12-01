package com.doofcraft.vessel.common.base

import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.ModComponents
import com.doofcraft.vessel.common.tooltip.TooltipRegistry
import net.fabricmc.fabric.api.item.v1.EnchantingContext
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.Enchantments

open class VesselBaseTool : Item(Properties()) {
    override fun getDescriptionId(stack: ItemStack): String? {
        val tag = stack.get(VesselTag.COMPONENT) ?: return super.getDescriptionId()
        return "item.${tag.key}"
    }

    override fun appendHoverText(
        stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component?>, tooltipFlag: TooltipFlag
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

    override fun isEnchantable(stack: ItemStack): Boolean {
        return true
    }

    override fun canBeEnchantedWith(
        stack: ItemStack, enchantment: Holder<Enchantment>, context: EnchantingContext
    ): Boolean {
        if (enchantment.`is`(Enchantments.BANE_OF_ARTHROPODS) || enchantment.`is`(Enchantments.SWEEPING_EDGE)) {
            return false
        }
        // Don't allow mending for a stack with no durability.
        if (!stack.has(DataComponents.MAX_DAMAGE) && enchantment.`is`(Enchantments.MENDING)) return false
        return super.canBeEnchantedWith(stack, enchantment, context)
    }
}