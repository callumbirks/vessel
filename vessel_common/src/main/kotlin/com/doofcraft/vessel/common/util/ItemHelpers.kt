package com.doofcraft.vessel.common.util

import com.doofcraft.vessel.common.api.VesselBehaviourRegistry
import com.doofcraft.vessel.common.api.VesselEvents
import com.doofcraft.vessel.common.api.event.ItemFinishUsingEvent
import com.doofcraft.vessel.common.api.event.ItemUseEvent
import com.doofcraft.vessel.common.api.event.ItemUseOnEntityEvent
import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.BehaviourComponents
import com.doofcraft.vessel.common.registry.StackComponents
import com.doofcraft.vessel.common.tooltip.TooltipRegistry
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level

object ItemHelpers {
    fun getDescriptionId(stack: ItemStack, fallback: (ItemStack) -> String?): String? {
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
        stack: ItemStack, entity: LivingEntity, fallback: (ItemStack, LivingEntity) -> Int
    ): Int {
        val tag = stack.get(VesselTag.COMPONENT) ?: return fallback(stack, entity)
        val anim =
            VesselBehaviourRegistry.get(tag.key, BehaviourComponents.ANIMATED_USE) ?: return fallback(stack, entity)
        return anim.duration
    }

    fun getUseAnimation(stack: ItemStack, fallback: (ItemStack) -> UseAnim): UseAnim {
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

    fun itemUse(
        level: Level,
        player: Player,
        usedHand: InteractionHand,
        fallback: (Level, Player, InteractionHand) -> InteractionResultHolder<ItemStack>
    ): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(usedHand)

        VesselEvents.ITEM_USE.post(ItemUseEvent(level, player, usedHand, stack)) { event ->
            if (event.result.result.consumesAction()) {
                return event.result
            }
        }
        return fallback(level, player, usedHand)
    }

    fun useOnEntity(
        stack: ItemStack,
        player: Player,
        interactionTarget: LivingEntity,
        usedHand: InteractionHand
    ): InteractionResult {
        VesselEvents.ITEM_USE_ON_ENTITY.post(
            ItemUseOnEntityEvent(
                player.level(), player, interactionTarget, usedHand, stack
            )
        ) { event ->
            if (event.result.consumesAction()) return event.result
        }
        return InteractionResult.PASS
    }

    fun finishUsingItem(
        stack: ItemStack,
        level: Level,
        livingEntity: LivingEntity,
        fallback: (ItemStack, Level, LivingEntity) -> ItemStack
    ): ItemStack {
        VesselEvents.ITEM_FINISH_USING.post(ItemFinishUsingEvent(level, livingEntity, stack)) { event ->
            if (event.result.result.consumesAction())
                return event.result.`object`
        }
        return fallback(stack, level, livingEntity)
    }
}