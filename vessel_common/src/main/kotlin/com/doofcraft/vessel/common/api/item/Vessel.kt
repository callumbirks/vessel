package com.doofcraft.vessel.common.api.item

import com.doofcraft.vessel.common.component.ComponentEntry
import com.doofcraft.vessel.common.component.VesselTag
import net.minecraft.core.component.DataComponentType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike

abstract class Vessel(val tag: VesselTag): ItemStackFactory {
    protected abstract val baseItem: ItemLike
    private val components = mutableListOf<ComponentEntry<*>>(
        ComponentEntry(VesselTag.Companion.COMPONENT) { tag }
    )

    protected fun <T> addComponent(type: DataComponentType<T>, supplier: () -> T) {
        components.add(ComponentEntry(type, supplier))
    }

    override fun create(count: Int): ItemStack {
        val stack = ItemStack(baseItem, count)
        components.forEach { it.apply(stack) }
        return stack
    }
}