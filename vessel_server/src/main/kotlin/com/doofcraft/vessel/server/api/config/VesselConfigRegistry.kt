package com.doofcraft.vessel.server.api.config

import com.doofcraft.vessel.server.api.events.VesselEvents
import com.doofcraft.vessel.server.api.events.config.ConfigsLoadedEvent
import net.minecraft.resources.ResourceLocation

object VesselConfigRegistry {
    private val factories = linkedMapOf<ResourceLocation, ConfigFactory<*>>()
    var configsLoaded: Boolean = false
        private set

    fun <T> register(factory: ConfigFactory<T>) {
        factories[factory.id] = factory
    }

    fun reloadAll() {
        if (!configsLoaded) {
            VesselEvents.CONFIGS_LOADED.emit(ConfigsLoadedEvent(factories.keys.toList()))
        }
        configsLoaded = true
        for ((id, factory) in factories) {
            factory.reload()
        }
    }

    fun reload(id: ResourceLocation): Boolean {
        val factory = factories[id] ?: return false
        factory.reload()
        return true
    }

    fun configIds(): Collection<ResourceLocation> = factories.keys
}