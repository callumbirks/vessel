package com.doofcraft.vessel.common.serialization.adapters

import com.doofcraft.vessel.common.serialization.VesselJSON
import com.doofcraft.vessel.common.serialization.toGsonElement
import com.doofcraft.vessel.common.serialization.toKxElement
import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder

object GsonSerializer : KSerializer<JsonElement> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("com.google.gson.JsonElement")

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: JsonElement) {
        if (encoder !is JsonEncoder) error { "Only JsonEncoder can serialize Gson" }
        encoder.encodeJsonElement(value.toKxElement())
    }

    override fun deserialize(decoder: Decoder): JsonElement {
        if (decoder !is JsonDecoder) error { "Only JsonDecoder can deserialize Gson" }
        return decoder.decodeJsonElement().toGsonElement()
    }

    fun <T> codecToString(codec: Codec<T>, value: T): String {
        return VesselJSON.JSON.encodeToString(
            GsonSerializer,
            codec.encodeStart(VesselJSON.MINECRAFT_JSON_OPS, value)
                .getOrThrow()
        )
    }
}