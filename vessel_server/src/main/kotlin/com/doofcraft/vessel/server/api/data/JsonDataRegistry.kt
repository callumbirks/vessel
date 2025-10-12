package com.doofcraft.vessel.server.api.data

import com.doofcraft.vessel.server.api.reactive.SimpleObservable
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.minecraft.resources.ResourceLocation
import java.io.File
import java.io.InputStream
import java.util.concurrent.ExecutionException

interface JsonDataRegistry<T> {
    val id: ResourceLocation
    val observable: SimpleObservable<out JsonDataRegistry<T>>
    val json: Json
    val serializer: KSerializer<T>

    fun reload(data: Map<ResourceLocation, T>)

    @OptIn(ExperimentalSerializationApi::class)
    fun reload() {
        val result = mutableMapOf<ResourceLocation, T>()
        val dir = File("data/${id.namespace}/${id.path}")
        dir.mkdirs()
        for (file in dir.listFiles { it.extension == "json" }) {
            val id = ResourceLocation.fromNamespaceAndPath(this.id.namespace, file.nameWithoutExtension)
            result[id] = parse(file.inputStream(), id)
        }
        this.reload(result)
        VesselMod.LOGGER.info("Reloaded the {} registry", this.id.toString())
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun parse(stream: InputStream, identifier: ResourceLocation): T {
        return try {
            json.decodeFromStream(serializer, stream)
        } catch (exception: Exception) {
            throw ExecutionException("Error loading JSON for data: $identifier", exception)
        }
    }
}