package com.doofcraft.vessel.common.serialization

import com.google.gson.JsonElement
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.data.registries.VanillaRegistries

object VesselJSON {
    val JSON = Json { ignoreUnknownKeys = true }

    val MINECRAFT_JSON_OPS: DynamicOps<JsonElement>
        get() {
            if (ops == null) ops = VanillaRegistries.createLookup().createSerializationContext(JsonOps.INSTANCE)
            return ops!!
        }

    init {
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { _, _, _ ->
            ops = VanillaRegistries.createLookup().createSerializationContext(JsonOps.INSTANCE)
        }
    }

    private var ops: DynamicOps<JsonElement>? = null
}