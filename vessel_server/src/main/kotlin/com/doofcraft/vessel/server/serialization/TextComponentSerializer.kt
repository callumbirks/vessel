package com.doofcraft.vessel.server.serialization

import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.kyori.adventure.text.serializer.ComponentSerializer
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.chat.Component as McComponent
import net.kyori.adventure.text.Component as AdvComponent
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.data.registries.VanillaRegistries

class TextComponentSerializer : ComponentSerializer<AdvComponent, AdvComponent, McComponent> {
    override fun deserialize(input: McComponent): AdvComponent {
        val json = ComponentSerialization.CODEC.encodeStart(jsonOps, input).getOrThrow()
        return componentJson.deserializeFromTree(json)
    }

    override fun serialize(component: AdvComponent): McComponent {
        val json = componentJson.serializeToTree(component)
        return ComponentSerialization.CODEC.decode(jsonOps, json).getOrThrow(::JsonParseException).first
    }

    companion object {
        private val jsonOps: DynamicOps<JsonElement>
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

        private val componentJson by lazy {
            GsonComponentSerializer.gson()
        }
    }
}