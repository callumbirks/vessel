package com.doofcraft.vessel.util.component

import net.minecraft.component.ComponentType
import net.minecraft.item.ItemStack

data class ComponentEntry<T>(
    val type: ComponentType<T>,
    val supplier: () -> T
) {
    fun apply(stack: ItemStack) {
        stack.set(type, supplier())
    }
}
