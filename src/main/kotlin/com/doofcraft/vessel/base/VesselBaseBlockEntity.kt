package com.doofcraft.vessel.base

import com.doofcraft.vessel.VesselMod
import com.doofcraft.vessel.registry.ModBlockEntities
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos

class VesselBaseBlockEntity(pos: BlockPos, state: BlockState): BlockEntity(ModBlockEntities.VESSEL, pos, state) {
    var item: ItemStack = ItemStack.EMPTY
        private set

    fun initialize(item: ItemStack) {
        if (!this.item.isEmpty) {
            VesselMod.LOGGER.warn("BlockEntity initialized multiple times")
        }
        this.item = item
    }
}