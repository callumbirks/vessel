package com.doofcraft.vessel.registry

import com.doofcraft.vessel.base.VesselBaseBlockEntity
import com.doofcraft.vessel.util.registry.SimpleRegistry
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.entity.BlockEntityType

object ModBlockEntities : SimpleRegistry<Registry<BlockEntityType<*>>, BlockEntityType<*>>() {
    override val registry: Registry<BlockEntityType<*>> = BuiltInRegistries.BLOCK_ENTITY_TYPE

    val VESSEL = create(
        "block_entity",
        BlockEntityType.Builder.of({ pos, state -> VesselBaseBlockEntity(pos, state) }, ModBlocks.VESSEL).build()
    )
}