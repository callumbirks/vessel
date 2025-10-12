package com.doofcraft.vessel.server.ui

import com.doofcraft.vessel.server.api.reactive.SimpleObservable
import com.doofcraft.vessel.server.ui.model.MenuDefinition
import com.doofcraft.vessel.common.util.vesselResource
import com.doofcraft.vessel.server.api.data.JsonDataRegistry
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import net.minecraft.resources.ResourceLocation

object MenuRegistry : JsonDataRegistry<MenuDefinition> {
    override val id: ResourceLocation = vesselResource("menus")
    override val serializer: KSerializer<MenuDefinition> = MenuDefinition.serializer()
    override val json: Json = Json { ignoreUnknownKeys = true }
    override val observable = SimpleObservable<MenuRegistry>()

    private val menus = hashMapOf<String, MenuDefinition>()

    override fun reload(data: Map<ResourceLocation, MenuDefinition>) {
        this.menus.clear()
        data.forEach { (identifier, menu) ->
            try {
                menu.id = identifier.path
                this.menus[identifier.path] = menu
            } catch (e: Exception) {
                VesselMod.LOGGER.error("Skipped loading the {} menu", identifier, e)
            }
        }
        VesselMod.LOGGER.info("Loaded {} menus", this.menus.size)
        this.observable.emit(this)
    }

    fun get(id: String): MenuDefinition? = this.menus[id]

    fun getOrThrow(id: String): MenuDefinition = this.menus[id] ?: throw NoSuchElementException("No such menu '$id'")
}