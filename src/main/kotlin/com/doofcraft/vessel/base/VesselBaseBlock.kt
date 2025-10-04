package com.doofcraft.vessel.base

import com.doofcraft.vessel.VesselMod
import com.doofcraft.vessel.api.VesselEvents
import com.doofcraft.vessel.api.VesselRegistry
import com.doofcraft.vessel.component.VesselTag
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class VesselBaseBlock(properties: Properties): BaseEntityBlock(properties) {
    override fun codec(): MapCodec<out BaseEntityBlock> = simpleCodec(::VesselBaseBlock)

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return VesselBaseBlockEntity(pos, state)
    }

    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        if (level.isClientSide) return

        val entity = level.getBlockEntity(pos) as VesselBaseBlockEntity? ?: run {
            VesselMod.LOGGER.trace("VesselBaseBlock placed but no BlockEntity found at pos")
            return
        }

        entity.item.get(VesselTag.COMPONENT)?.let { tag ->
            val block = VesselRegistry.getBlock(tag.key)
                ?: return@let
            block.onPlaced(level as ServerLevel, pos, placer, entity)
            VesselEvents.BLOCK_ENTITY_LOAD.invoker().onLoad(entity, level)
        }
    }

    override fun onRemove(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        newState: BlockState,
        movedByPiston: Boolean
    ) {
        val entity = level.getBlockEntity(pos) ?: run {
            VesselMod.LOGGER.trace("VesselBaseBlock destroyed but no BlockEntity found at pos")
            return super.onRemove(state, level, pos, newState, movedByPiston)
        }

        if (entity is VesselBaseBlockEntity) {
            entity.item.get(VesselTag.COMPONENT)?.let {
                val block = VesselRegistry.getBlock(it.key) ?: return@let
                block.onDestroyed(level, pos, entity)
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston)
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.INVISIBLE
    }
}