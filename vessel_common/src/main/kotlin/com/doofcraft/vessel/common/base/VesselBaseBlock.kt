package com.doofcraft.vessel.common.base

import com.doofcraft.vessel.common.api.VesselEvents
import com.doofcraft.vessel.common.api.event.BlockDestroyedEvent
import com.doofcraft.vessel.common.api.event.BlockInteractEvent
import com.doofcraft.vessel.common.api.event.BlockPlacedEvent
import com.doofcraft.vessel.common.component.VesselTag
import com.mojang.serialization.MapCodec
import net.minecraft.client.particle.BreakingItemParticle
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class VesselBaseBlock(properties: Properties) : BaseEntityBlock(properties) {
    override fun codec(): MapCodec<out BaseEntityBlock> = simpleCodec(::VesselBaseBlock)

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return VesselBaseBlockEntity(pos, state)
    }

    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        super.setPlacedBy(level, pos, state, placer, stack)
        val be = level.getBlockEntity(pos) as VesselBaseBlockEntity? ?: return
        VesselEvents.BLOCK_PLACED.emit(BlockPlacedEvent(level, pos, placer, be))
    }

    override fun onRemove(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        newState: BlockState,
        movedByPiston: Boolean
    ) {
        val be = level.getBlockEntity(pos) as VesselBaseBlockEntity?
        if (be != null) {
            VesselEvents.BLOCK_DESTROYED.emit(BlockDestroyedEvent(level, pos, be))
        }

        super.onRemove(state, level, pos, newState, movedByPiston)
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

    override fun spawnDestroyParticles(level: Level, player: Player, pos: BlockPos, state: BlockState) {
        if (!level.isClientSide) return
        val be = level.getBlockEntity(pos) as? VesselBaseBlockEntity ?: return
        val stack = be.item
        if (stack.isEmpty) return

        val random = level.random

        repeat(32) {
            val x = pos.x.toDouble() + 0.5 + (random.nextDouble() - 0.5)
            val y = pos.y.toDouble() + 0.5 + (random.nextDouble() - 0.5)
            val z = pos.z.toDouble() + 0.5 + (random.nextDouble() - 0.5)

            val vx = (random.nextDouble() - 0.5) * 0.2
            val vy = random.nextDouble() * 0.2
            val vz = (random.nextDouble() - 0.5) * 0.2

            level.addParticle(
                ItemParticleOption(ParticleTypes.ITEM, stack),
                x, y, z,
                vx, vy, vz
            )
        }
    }
}