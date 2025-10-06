package com.doofcraft.vessel.api.data

import com.doofcraft.vessel.VesselMod
import com.doofcraft.vessel.util.endsWith
import com.google.common.reflect.TypeToken
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.util.concurrent.ExecutionException

/**
 * A [DataRegistry] that consumes JSON files.
 * Every deserialized instance is attached to an [ResourceLocation].
 * For example a file under data/mymod/[resourcePath]/entry.json would be backed by the identifier modid:entry.
 *
 * @param T The type of the data consumed by this registry.
 */
interface JsonDataRegistry<T> : DataRegistry {
    /**
     * The [Json] used to deserialize the data this registry will consume.
     */
    val json: Json

    /**
     * The [KSerializer] for [T].
     */
    val serializer: KSerializer<T>

    /**
     * The folder location for the data this registry will consume.
     */
    val resourcePath: String

    override fun reload(manager: ResourceManager) {
        val data = hashMapOf<ResourceLocation, T>()
        manager.listResources(resourcePath) { path -> path.endsWith(JSON_EXTENSION) }
            .forEach { (identifier, resource) ->
                resource.open().use { stream ->
                    val resolvedId = ResourceLocation.fromNamespaceAndPath(
                        identifier.namespace, File(identifier.path).nameWithoutExtension
                    )
                    data[resolvedId] = parse(stream, resolvedId)
                }
            }

        reload(data)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun parse(stream: InputStream, identifier: ResourceLocation): T {
        return try {
            json.decodeFromStream(serializer, stream)
        } catch (exception: Exception) {
            throw ExecutionException("Error loading JSON for data: $identifier", exception)
        }
    }

    /**
     * Reloads this registry from the deserialized data.
     *
     * @param data A map of the data associating an instance to the respective identifier from the [ResourceManager].
     */
    fun reload(data: Map<ResourceLocation, T>)

    companion object {
        const val JSON_EXTENSION = ".json"
    }
}
