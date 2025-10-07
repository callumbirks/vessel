package com.doofcraft.vessel.common.component

import net.minecraft.core.component.DataComponentType
import net.minecraft.world.item.ItemStack

data class ComponentEntry<T>(
    val type: DataComponentType<T>,
    val supplier: () -> T
) {
    fun apply(stack: ItemStack) {
        stack.set(type, supplier())
    }
}