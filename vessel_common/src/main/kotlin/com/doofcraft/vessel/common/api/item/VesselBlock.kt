package com.doofcraft.vessel.common.api.item

import com.doofcraft.vessel.common.base.VesselBaseBlockEntity
import com.doofcraft.vessel.common.component.BlockShapeComponent
import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.BehaviourComponents
import com.doofcraft.vessel.common.registry.ModItems
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.LevelAccessor
import kotlin.math.abs

abstract class VesselBlock(
    tag: VesselTag,
    shape: BlockShapeComponent? = null,
) : Vessel(tag) {
    override val baseItem: ItemLike = ModItems.BLOCK_ITEM

    init {
        if (shape != null && shape.isValid()) {
            addBehaviour(BehaviourComponents.BLOCK_SHAPE) { shape }
        }
    }

    open fun use(
        level: ServerLevel, entity: VesselBaseBlockEntity, player: ServerPlayer, hand: InteractionHand
    ): InteractionResult {
        return InteractionResult.PASS
    }

    open fun onPlaced(level: ServerLevel, pos: BlockPos, placer: LivingEntity?, entity: VesselBaseBlockEntity) {
    }

    open fun onDestroyed(level: LevelAccessor, pos: BlockPos, entity: VesselBaseBlockEntity) {
    }
}