package com.doofcraft.vessel.registry

import com.doofcraft.vessel.component.VesselTag
import com.doofcraft.vessel.util.registry.SimpleRegistry
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries

object ModComponents : SimpleRegistry<Registry<DataComponentType<*>>, DataComponentType<*>>() {
    override val registry: Registry<DataComponentType<*>> = BuiltInRegistries.DATA_COMPONENT_TYPE

    val VESSEL_TAG: DataComponentType<VesselTag> =
        create("tag", DataComponentType.builder<VesselTag>().persistent(VesselTag.CODEC).build())
}