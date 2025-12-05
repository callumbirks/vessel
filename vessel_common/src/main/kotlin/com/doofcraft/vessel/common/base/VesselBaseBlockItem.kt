package com.doofcraft.vessel.common.base

import com.doofcraft.vessel.common.registry.ModBlocks
import com.doofcraft.vessel.common.util.ItemHelpers
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.state.BlockState
import kotlin.math.round

class VesselBaseBlockItem : BlockItem(ModBlocks.VESSEL, Properties()) {
    override fun getDescriptionId(stack: ItemStack): String? {
        return ItemHelpers.getDescriptionId(stack) { super.getDescriptionId(stack) }
    }

    override fun appendHoverText(
        stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component?>, tooltipFlag: TooltipFlag
    ) {
        return ItemHelpers.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
    }

    override fun placeBlock(context: BlockPlaceContext, state: BlockState): Boolean {
        val result = super.placeBlock(context, state)
        if (result) {
            val blockEntity = context.level.getBlockEntity(context.clickedPos) as VesselBaseBlockEntity
            var yaw = 0f
            context.player?.let { player ->
                yaw = round(Mth.wrapDegrees(player.yRot + 180f) / 45f) * 45f
            }
            blockEntity.initialize(context.itemInHand, yaw)
        }
        return result
    }
}