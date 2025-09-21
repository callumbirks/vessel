package com.doofcraft.vessel.registry

import com.doofcraft.vessel.base.VesselBaseBlockItem
import com.doofcraft.vessel.base.VesselBaseItem
import com.doofcraft.vessel.util.registry.SimpleRegistry
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.Item

object ModItems : SimpleRegistry<Registry<Item>, Item>() {
    override val registry: Registry<Item> = BuiltInRegistries.ITEM

    val VESSEL = create("item", VesselBaseItem())
    val VESSEL_BLOCK = create("block_item", VesselBaseBlockItem())
}