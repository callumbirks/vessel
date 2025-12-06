package com.doofcraft.vessel.common.component

import com.doofcraft.vessel.common.api.VesselBehaviourRegistry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.PatchedDataComponentMap
import net.minecraft.world.item.ItemStack

data class ComponentEntry<T>(
    val type: DataComponentType<T>,
    val supplier: () -> T
) {
    fun apply(stack: ItemStack) {
        stack.set(type, supplier())
    }

    fun apply(map: PatchedDataComponentMap) {
        map.set(type, supplier())
    }

    fun registerBehaviour(key: String) {
        VesselBehaviourRegistry.set(key, type, supplier())
    }
}