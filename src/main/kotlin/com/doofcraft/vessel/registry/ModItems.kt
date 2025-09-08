package com.doofcraft.vessel.registry

import com.doofcraft.vessel.base.VesselBaseBlockItem
import com.doofcraft.vessel.base.VesselBaseItem
import com.doofcraft.vessel.util.registry.SimpleRegistry
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object ModItems : SimpleRegistry<Registry<Item>, Item>() {
    override val registry: Registry<Item> = Registries.ITEM

    val VESSEL = create("item", VesselBaseItem())
    val VESSEL_BLOCK = create("block_item", VesselBaseBlockItem())
}