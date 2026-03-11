package com.doofcraft.vessel.server.api.config

import com.doofcraft.vessel.server.api.events.VesselServerEvents
import com.doofcraft.vessel.server.api.events.config.ConfigsLoadedEvent
import com.doofcraft.vessel.server.api.data.Result
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
            VesselServerEvents.CONFIGS_LOADED.emit(ConfigsLoadedEvent(factories.keys.toList()))
        }
        configsLoaded = true
    }

    fun initialValidate() {
        for ((id, factory) in factories) {
            factory.validate()
        }
    }

    fun reload(id: ResourceLocation): Result<Unit> {
        val factory = factories[id] ?: return Result.failure("No such config '$id'")
        factory.reload()
        factory.validate().then { return Result.failure(it) }
        return Result.success(Unit)
    }

    fun configIds(): Collection<ResourceLocation> = factories.keys
}