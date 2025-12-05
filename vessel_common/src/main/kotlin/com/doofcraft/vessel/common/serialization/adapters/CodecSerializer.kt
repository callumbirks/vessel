package com.doofcraft.vessel.common.serialization.adapters

import com.doofcraft.vessel.common.serialization.VesselJSON
import com.google.gson.JsonParseException
import com.mojang.serialization.Codec
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

abstract class CodecSerializer<T>(val serialName: String, val codec: Codec<T>) : KSerializer<T> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(serialName)

    override fun serialize(encoder: Encoder, value: T) {
        val json = codec.encodeStart(VesselJSON.MINECRAFT_JSON_OPS, value).getOrThrow()
        encoder.encodeSerializableValue(GsonSerializer, json)
    }

    override fun deserialize(decoder: Decoder): T {
        val json = decoder.decodeSerializableValue(GsonSerializer)
        return codec.decode(VesselJSON.MINECRAFT_JSON_OPS, json).getOrThrow(::JsonParseException).first
    }
}