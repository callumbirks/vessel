package com.doofcraft.vessel.common.base

import com.doofcraft.vessel.common.registry.StackComponents
import net.fabricmc.fabric.api.item.v1.EnchantingContext
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.Enchantments

open class VesselBaseTool : VesselBaseItem() {
    // Same as diamond tier
    override fun getEnchantmentValue(): Int = 10

    // Copy some behaviours from DiggerItem / TieredItem
    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        return true
    }

    // Same as TieredItem but using components.
    override fun isValidRepairItem(stack: ItemStack, repairCandidate: ItemStack): Boolean {
        val data = stack.get(StackComponents.INGREDIENT) ?: return false
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