package com.doofcraft.vessel.common.api.item

import com.doofcraft.vessel.common.api.VesselBehaviourRegistry
import com.doofcraft.vessel.common.component.ComponentEntry
import com.doofcraft.vessel.common.component.VesselTag
import net.minecraft.core.component.DataComponentMap
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.PatchedDataComponentMap
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike

abstract class Vessel(val tag: VesselTag): ItemStackFactory {
    protected abstract val baseItem: ItemLike
    private val componentFactory = mutableListOf<ComponentEntry<*>>(
        ComponentEntry(VesselTag.COMPONENT) { tag }
    )
    private val behaviours = mutableListOf<ComponentEntry<*>>()

    protected fun <T> addComponent(type: DataComponentType<T>, supplier: () -> T) {
        componentFactory.add(ComponentEntry(type, supplier))
    }

    protected fun <T> addBehaviour(type: DataComponentType<T>, supplier: () -> T) {
        behaviours.add(ComponentEntry(type, supplier))
    }

    fun registerBehaviours() {
        if (behaviours.isEmpty()) return
        val map = PatchedDataComponentMap(DataComponentMap.EMPTY)
        behaviours.forEach { it.apply(map) }
        // Clear behaviours as we only need to register once per item type.
        behaviours.clear()
        VesselBehaviourRegistry.set(tag.key, map)
    }

    override fun create(count: Int): ItemStack {
        val stack = ItemStack(baseItem, count)
        componentFactory.forEach { it.apply(stack) }
        return stack
    }
}