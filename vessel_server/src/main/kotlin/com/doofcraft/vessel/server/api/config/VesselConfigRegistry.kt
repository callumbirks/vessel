package com.doofcraft.vessel.server.api.config

import com.doofcraft.vessel.common.api.VesselBehaviourRegistry
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

    fun initialReload() {
        if (configsLoaded) return
        for ((id, factory) in factories) {
            factory.reload()
        }
        if (!configsLoaded) {
            VesselEvents.CONFIGS_LOADED.emit(ConfigsLoadedEvent(factories.keys.toList()))
        }
        configsLoaded = true
        // Components most likely change when configs are reloaded, so sync.
        VesselBehaviourRegistry.syncToPlayers()
    }

    fun initialValidate() {
        for ((id, factory) in factories) {
            factory.validate()
        }
    }

    fun reload(id: ResourceLocation): Boolean {
        val factory = factories[id] ?: return false
        factory.reload()
        factory.validate()
        VesselBehaviourRegistry.syncToPlayers()
        return true
    }

    fun configIds(): Collection<ResourceLocation> = factories.keys
}