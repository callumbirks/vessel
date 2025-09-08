package com.doofcraft.vessel.base

import com.doofcraft.vessel.VesselMod
import com.mojang.serialization.MapCodec
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class VesselBaseBlock(settings: Settings): BlockWithEntity(settings) {
    override fun getCodec(): MapCodec<out BlockWithEntity> = createCodec(::VesselBaseBlock)

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return VesselBaseBlockEntity(pos, state)
    }

    override fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack
    ) {
        val entity = world.getBlockEntity(pos) ?: run {
            VesselMod.LOGGER.trace("VesselBaseBlock placed but no BlockEntity found at pos")
            return
        }

        if (entity is VesselBaseBlockEntity) {
            entity.initialize(itemStack)
        }
    }
}