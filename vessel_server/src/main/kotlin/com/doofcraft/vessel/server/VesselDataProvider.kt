package com.doofcraft.vessel.server

import VesselMod.LOGGER
import com.doofcraft.vessel.server.api.data.JsonDataRegistry
import com.doofcraft.vessel.server.ui.MenuRegistry
import net.minecraft.resources.ResourceLocation

object VesselDataProvider {
    private val registries = linkedSetOf<JsonDataRegistry<*>>()

    fun registerDefaults() {
        register(MenuRegistry)
    }

    fun reloadAll() {
        this.registries.forEach { registry ->
            registry.reload()
        }
    }

    fun reload(identifier: ResourceLocation): Boolean {
        val registry = this.registries.find { it.id == identifier }
        if (registry == null) return false
        registry.reload()
        return true
    }

    fun <T : JsonDataRegistry<*>> register(registry: T): T {
        this.registries.add(registry)
        LOGGER.info("Registered the {} registry", registry.id.toString())
        LOGGER.debug("Registered the {} registry of class {}", registry.id.toString(), registry::class.qualifiedName)
        return registry
    }

    fun getRegistry(registryIdentifier: ResourceLocation): JsonDataRegistry<*>? =
        this.registries.find { it.id == registryIdentifier }

    fun allRegistries(): Collection<JsonDataRegistry<*>> = registries
}
