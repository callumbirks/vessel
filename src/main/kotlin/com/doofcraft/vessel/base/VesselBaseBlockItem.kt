package com.doofcraft.vessel.base

import com.doofcraft.vessel.component.VesselTag
import com.doofcraft.vessel.registry.ModBlocks
import net.minecraft.util.Mth
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.state.BlockState

class VesselBaseBlockItem: BlockItem(ModBlocks.VESSEL, Properties()) {
    override fun getDescriptionId(stack: ItemStack): String {
        val tag = stack.get(VesselTag.COMPONENT) ?: return super.getDescriptionId()
        return "item.${tag.key}"
    }

    override fun placeBlock(context: BlockPlaceContext, state: BlockState): Boolean {
        val result = super.placeBlock(context, state)
        if (result) {
            val blockEntity = context.level.getBlockEntity(context.clickedPos) as VesselBaseBlockEntity
            var yaw = 0f
            context.player?.let { player ->
                yaw = kotlin.math.round(Mth.wrapDegrees(player.yRot + 180f) / 45f) * 45f
            }
            blockEntity.initialize(context.itemInHand, yaw)
        }
        return result
    }
}