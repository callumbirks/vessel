package com.doofcraft.vessel.common.base

import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.ModComponents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemUtils
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level

open class VesselBaseItem(): Item(Properties()) {
    override fun getDescriptionId(stack: ItemStack): String {
        val tag = stack.get(VesselTag.COMPONENT) ?: return super.getDescriptionId()
        return "item.${tag.key}"
    }

    override fun getUseAnimation(stack: ItemStack): UseAnim? {
        val consumable = stack.get(ModComponents.CONSUMABLE)
            ?: return super.getUseAnimation(stack)
        return consumable.animation
    }

    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int {
        val consumable = stack.get(ModComponents.CONSUMABLE)
            ?: return super.getUseDuration(stack, entity)
        return consumable.duration
    }

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(usedHand)
        return if (stack.has(ModComponents.CONSUMABLE)) {
            ItemUtils.startUsingInstantly(level, player, usedHand)
        } else {
            super.use(level, player, usedHand)
        }
    }
}