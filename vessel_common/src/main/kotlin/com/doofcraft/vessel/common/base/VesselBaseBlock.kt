package com.doofcraft.vessel.common.base

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class VesselBaseBlock(properties: Properties): BaseEntityBlock(properties) {
    override fun codec(): MapCodec<out BaseEntityBlock> = simpleCodec(::VesselBaseBlock)

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return VesselBaseBlockEntity(pos, state)
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.INVISIBLE
    }

    override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape? {
        val be = level.getBlockEntity(pos) as? VesselBaseBlockEntity ?: return Shapes.block()
        return be.shape ?: Shapes.block()
    }

    override fun getCollisionShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape? {
        val be = level.getBlockEntity(pos) as? VesselBaseBlockEntity ?: return Shapes.block()
        return be.shape ?: Shapes.block()
    }

    override fun getOcclusionShape(state: BlockState, level: BlockGetter, pos: BlockPos): VoxelShape? {
        val be = level.getBlockEntity(pos) as? VesselBaseBlockEntity ?: return Shapes.block()
        return be.shape ?: Shapes.block()
    }

    override fun getVisualShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape? {
        val be = level.getBlockEntity(pos) as? VesselBaseBlockEntity ?: return Shapes.block()
        return be.shape ?: Shapes.block()
    }

    override fun getBlockSupportShape(state: BlockState, level: BlockGetter, pos: BlockPos): VoxelShape? {
        val be = level.getBlockEntity(pos) as? VesselBaseBlockEntity ?: return Shapes.block()
        return be.shape ?: Shapes.block()
    }
}