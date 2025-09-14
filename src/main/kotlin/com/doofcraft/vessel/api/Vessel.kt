package com.doofcraft.vessel.api

import com.doofcraft.vessel.component.VesselTag
import com.doofcraft.vessel.util.component.ComponentEntry
import net.minecraft.component.ComponentType
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack

abstract class Vessel(val tag: VesselTag): ItemStackFactory {
    protected abstract val baseItem: ItemConvertible
    private val components = mutableListOf<ComponentEntry<*>>(
        ComponentEntry(VesselTag.COMPONENT, tag)
    )

    protected fun <T> addComponent(type: ComponentType<T>, value: T) {
        components.add(ComponentEntry(type, value))
    }

    override fun create(count: Int): ItemStack {
        val stack = ItemStack(baseItem, count)
        components.forEach { it.apply(stack) }
        return stack
    }
}