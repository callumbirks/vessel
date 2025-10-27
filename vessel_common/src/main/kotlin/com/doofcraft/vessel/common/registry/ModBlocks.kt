package com.doofcraft.vessel.common.registry

import com.doofcraft.vessel.common.base.VesselBaseBlock
import com.doofcraft.vessel.common.base.VesselBaseBlockEntity
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor

object ModBlocks : SimpleRegistry<Registry<Block>, Block>() {
    override val registry: Registry<Block> = BuiltInRegistries.BLOCK

    val VESSEL = create(
        "block", VesselBaseBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.EMERALD)
                .strength(0.8f)
                .noOcclusion()
                .dynamicShape()
                .isSuffocating { state, level, pos ->
                    val be = level.getBlockEntity(pos) as? VesselBaseBlockEntity
                    be?.shape == null
                })
    )
}