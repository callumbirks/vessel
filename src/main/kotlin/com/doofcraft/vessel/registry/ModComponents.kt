package com.doofcraft.vessel.registry

import com.doofcraft.vessel.component.VesselTag
import com.doofcraft.vessel.util.registry.SimpleRegistry
import net.minecraft.component.ComponentType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object ModComponents : SimpleRegistry<Registry<ComponentType<*>>, ComponentType<*>>() {
    override val registry: Registry<ComponentType<*>> = Registries.DATA_COMPONENT_TYPE

    val VESSEL_TAG: ComponentType<VesselTag> =
        create("tag", ComponentType.builder<VesselTag>().codec(VesselTag.CODEC).build())
}