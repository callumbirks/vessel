package com.doofcraft.vessel.util.registry

import com.doofcraft.vessel.VesselMod.vesselResource
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

abstract class SimpleRegistry<R: Registry<T>, T> {
    abstract val registry: R

    private val queue = hashMapOf<Identifier, T>()

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