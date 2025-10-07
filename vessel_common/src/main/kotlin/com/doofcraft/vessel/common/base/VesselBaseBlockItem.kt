package com.doofcraft.vessel.common.base

import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.ModBlocks
import net.minecraft.util.Mth
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.state.BlockState
import kotlin.math.round

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
                yaw = round(Mth.wrapDegrees(player.yRot + 180f) / 45f) * 45f
            }
            blockEntity.initialize(context.itemInHand, yaw)
        }
        return result
    }
}