package com.doofcraft.vessel.common.base

import com.doofcraft.vessel.common.api.VesselEvents
import com.doofcraft.vessel.common.api.event.ItemSlotClickedEvent
import com.doofcraft.vessel.common.util.ItemHelpers
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.SlotAccess
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ClickAction
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
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
        return ItemHelpers.itemUse(level, player, usedHand) { level, player, usedHand ->
            super.use(
                level, player, usedHand
            )
        }
    }

    override fun overrideOtherStackedOnMe(
        stack: ItemStack,
        other: ItemStack,
        slot: Slot,
        action: ClickAction,
        player: Player,
        access: SlotAccess
    ): Boolean {
        VesselEvents.ITEM_SLOT_CLICKED.post(ItemSlotClickedEvent(player, slot, action, stack, other)) { event ->
            if (event.result.consumesAction()) return true
        }
        return super.overrideOtherStackedOnMe(stack, other, slot, action, player, access)
    }

    override fun interactLivingEntity(
        stack: ItemStack, player: Player, interactionTarget: LivingEntity, usedHand: InteractionHand
    ): InteractionResult {
        return ItemHelpers.useOnEntity(stack, player, interactionTarget, usedHand)
    }

    override fun finishUsingItem(stack: ItemStack, level: Level, livingEntity: LivingEntity): ItemStack? {
        return ItemHelpers.finishUsingItem(
            stack, level, livingEntity
        ) { stack, level, livingEntity -> super.finishUsingItem(stack, level, livingEntity) }
    }
}