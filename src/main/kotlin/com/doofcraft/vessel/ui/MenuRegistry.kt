package com.doofcraft.vessel.ui

import com.doofcraft.vessel.api.data.DataRegistry
import com.doofcraft.vessel.api.data.JsonDataRegistry
import com.doofcraft.vessel.api.reactive.SimpleObservable
import com.doofcraft.vessel.ui.model.MenuDefinition
import com.doofcraft.vessel.util.vesselResource
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType

object MenuRegistry : JsonDataRegistry<MenuDefinition> {
    override val id: ResourceLocation = vesselResource("menus")
    override val type: PackType = PackType.SERVER_DATA
    override val serializer: KSerializer<MenuDefinition> = MenuDefinition.serializer()
    override val json: Json = Json { ignoreUnknownKeys = true }
    override val observable = SimpleObservable<MenuRegistry>()
    override val resourcePath: String = "menus"

    private val menus = hashMapOf<ResourceLocation, MenuDefinition>()

    override fun reload(data: Map<ResourceLocation, MenuDefinition>) {
        this.menus.clear()
        data.forEach { (identifier, menu) ->
            try {

            }
        }
    }

    override fun sync(player: ServerPlayer) {
        TODO("Not yet implemented")
    }
}