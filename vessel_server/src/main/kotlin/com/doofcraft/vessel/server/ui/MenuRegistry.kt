package com.doofcraft.vessel.server.ui

import com.doofcraft.vessel.common.VesselMod
import com.doofcraft.vessel.common.reactive.SimpleObservable
import com.doofcraft.vessel.common.util.vesselResource
import com.doofcraft.vessel.server.api.data.JsonDataRegistry
import com.doofcraft.vessel.server.ui.model.MenuDefinition
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
                val canonicalId = identifier.toString()
                menu.id = canonicalId
                this.menus[canonicalId] = menu
            } catch (e: Exception) {
                VesselMod.LOGGER.error("Skipped loading the {} menu", identifier, e)
            }
        }
        VesselMod.LOGGER.info("Loaded {} menus", this.menus.size)
        this.observable.emit(this)
    }

    fun normalizeId(id: String): String = if (':' in id) id else "${VesselMod.MODID}:$id"

    fun get(id: String): MenuDefinition? = this.menus[normalizeId(id)]

    fun getOrThrow(id: String): MenuDefinition {
        val normalized = normalizeId(id)
        return this.menus[normalized] ?: throw NoSuchElementException("No such menu '$normalized'")
    }
}
