package com.doofcraft.vessel.common.registry

import com.doofcraft.vessel.common.util.vesselResource
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation

abstract class SimpleRegistry<R: Registry<T>, T: Any> {
    abstract val registry: R

    private val queue = hashMapOf<ResourceLocation, T>()

    open fun <E : T> create(name: String, item: E): E {
        queue[vesselResource(name)] = item
        return item
    }

    open fun register() {
        queue.forEach { (id, item) ->
            Registry.register(registry, id, item)
        }
    }
}