package com.doofcraft.vessel.common.util

import com.doofcraft.vessel.common.api.VesselBehaviourRegistry
import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.BehaviourComponents
import com.doofcraft.vessel.common.registry.StackComponents
import com.doofcraft.vessel.common.tooltip.TooltipRegistry
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim

object ItemHelpers {
    fun getDescriptionId(stack: ItemStack, fallback: (stack: ItemStack) -> String?): String? {
        val tag = stack.get(VesselTag.COMPONENT) ?: return fallback(stack)
        return "item.${tag.key}"
    }

    fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        tooltipComponents: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        val tag = stack.get(VesselTag.COMPONENT) ?: return
        val tooltips = TooltipRegistry.getTooltip(tag.key) ?: return
        tooltipComponents.addAll(tooltips)
    }

    fun getUseDuration(
        stack: ItemStack, entity: LivingEntity, fallback: (stack: ItemStack, entity: LivingEntity) -> Int
    ): Int {
        val tag = stack.get(VesselTag.COMPONENT) ?: return fallback(stack, entity)
        val anim =
            VesselBehaviourRegistry.get(tag.key, BehaviourComponents.ANIMATED_USE) ?: return fallback(stack, entity)
        return anim.duration
    }

    fun getUseAnimation(stack: ItemStack, fallback: (stack: ItemStack) -> UseAnim): UseAnim {
        val tag = stack.get(VesselTag.COMPONENT) ?: return fallback(stack)
        val anim = VesselBehaviourRegistry.get(tag.key, BehaviourComponents.ANIMATED_USE) ?: return fallback(stack)
        return anim.animation
    }

    fun removeExpiredCooldown(stack: ItemStack, server: MinecraftServer): Boolean {
        val cooldown = stack.get(StackComponents.COOLDOWN) ?: return false
        if (server.tickCount >= cooldown.endTick) {
            stack.remove(StackComponents.COOLDOWN)
            return true
        } else {
            return false
        }
    }
}