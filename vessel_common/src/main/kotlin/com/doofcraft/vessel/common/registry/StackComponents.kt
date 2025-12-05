package com.doofcraft.vessel.common.registry

import com.doofcraft.vessel.common.component.BlockShapeComponent
import com.doofcraft.vessel.common.component.IngredientComponent
import com.doofcraft.vessel.common.component.MenuButton
import com.doofcraft.vessel.common.component.VesselTag
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries

object StackComponents : SimpleRegistry<Registry<DataComponentType<*>>, DataComponentType<*>>() {
    override val registry: Registry<DataComponentType<*>> = BuiltInRegistries.DATA_COMPONENT_TYPE

    @JvmField
    val VESSEL_TAG: DataComponentType<VesselTag> =
        create("tag", DataComponentType.builder<VesselTag>().persistent(VesselTag.CODEC).build())

    // MenuButton data is not persisted, as items only live in temporary UI not permanent storage.
    @JvmField
    val MENU_BUTTON: DataComponentType<MenuButton> = create(
        "menu_button", DataComponentType.builder<MenuButton>().networkSynchronized(MenuButton.NOOP_CODEC).build()
    )

    @JvmField
    val BLOCK_SHAPE: DataComponentType<BlockShapeComponent> = create(
        "block_shape", DataComponentType.builder<BlockShapeComponent>().persistent(BlockShapeComponent.CODEC).build()
    )

    @JvmField
    val INGREDIENT: DataComponentType<IngredientComponent> = create(
        "ingredient", DataComponentType.builder<IngredientComponent>().persistent(IngredientComponent.CODEC).build()
    )
}