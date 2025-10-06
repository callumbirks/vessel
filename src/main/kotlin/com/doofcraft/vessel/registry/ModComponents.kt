package com.doofcraft.vessel.registry

import com.doofcraft.vessel.component.MenuButton
import com.doofcraft.vessel.component.VesselTag
import com.doofcraft.vessel.util.registry.SimpleRegistry
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries

object ModComponents : SimpleRegistry<Registry<DataComponentType<*>>, DataComponentType<*>>() {
    override val registry: Registry<DataComponentType<*>> = BuiltInRegistries.DATA_COMPONENT_TYPE

    @JvmField
    val VESSEL_TAG: DataComponentType<VesselTag> =
        create("tag", DataComponentType.builder<VesselTag>().persistent(VesselTag.CODEC).build())

    @JvmField
    val MENU_BUTTON: DataComponentType<MenuButton> = create(
        "menu_button", DataComponentType.builder<MenuButton>().persistent(
            MenuButton.CODEC
        ).build()
    )
}