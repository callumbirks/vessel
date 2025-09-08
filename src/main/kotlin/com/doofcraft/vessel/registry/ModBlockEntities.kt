package com.doofcraft.vessel.registry

import com.doofcraft.vessel.base.VesselBaseBlockEntity
import com.doofcraft.vessel.util.registry.SimpleRegistry
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object ModBlockEntities : SimpleRegistry<Registry<BlockEntityType<*>>, BlockEntityType<*>>() {
    override val registry: Registry<BlockEntityType<*>> = Registries.BLOCK_ENTITY_TYPE

    val VESSEL = create(
        "block_entity",
        BlockEntityType.Builder.create({ pos, state -> VesselBaseBlockEntity(pos, state) }, ModBlocks.VESSEL).build()
    )
}