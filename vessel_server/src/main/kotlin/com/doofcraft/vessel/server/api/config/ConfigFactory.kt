package com.doofcraft.vessel.server.api.config

import com.doofcraft.vessel.common.VesselMod
import com.doofcraft.vessel.common.reactive.SimpleObservable
import com.doofcraft.vessel.server.api.data.Result
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import net.minecraft.resources.ResourceLocation
import java.io.File

interface ConfigFactory<T> {
    val id: ResourceLocation
    val observable: SimpleObservable<T>
    val json: Json
    val serializer: KSerializer<T>

    @Suppress("PropertyName")
    var CONFIG: T

    fun reload(config: T)
    fun validate(config: T): Result<Unit>
    fun default(): T

    @OptIn(ExperimentalSerializationApi::class)
    fun reload() {
        var config = default()

        val configFile = File("config/${id.namespace}/${id.path}.json")
        configFile.parentFile.mkdirs()

        if (configFile.exists()) {
            try {
                configFile.inputStream().use { stream ->
                    config = json.decodeFromStream(serializer, stream)
                }
            } catch (e: Exception) {
                VesselMod.LOGGER.error("Error reading $id config: ${e.message}", e)
                config = default()
            }
        } else {
            configFile.outputStream().use { stream ->
                json.encodeToStream(serializer, config, stream)
            }
        }

        CONFIG = config
        reload(CONFIG)
    }

    fun validate() {
        validate(CONFIG).then(
            onSuccess = { VesselMod.LOGGER.info("Validated $id config successfully.") },
            onFailure = { VesselMod.LOGGER.warn("Failed to validate $id config: $it") })
    }
}