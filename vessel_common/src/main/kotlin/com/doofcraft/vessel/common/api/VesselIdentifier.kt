package com.doofcraft.vessel.common.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.ResourceLocationException
import net.minecraft.resources.ResourceLocation

@Serializable(with = VesselIdentifier.Companion.StringSerializer::class)
data class VesselIdentifier(
    val namespace: String? = null,
    val path: String,
) : Comparable<VesselIdentifier> {
    override fun toString(): String {
        return "${namespace ?: VesselMod.MODID}:$path"
    }

    fun toIdentifier(): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(namespace ?: VesselMod.MODID, path)
    }

    fun effectiveNamespace(): String = namespace ?: VesselMod.MODID

    override fun equals(other: Any?): Boolean {
        if (other is VesselIdentifier) {
            return this.compareTo(other) == 0
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return effectiveNamespace().hashCode()
    }

    override fun compareTo(other: VesselIdentifier): Int {
        var result = this.path.compareTo(other.path)
        if (result == 0) {
            result = this.effectiveNamespace().compareTo(other.effectiveNamespace())
        }

        return result
    }

    companion object {
        fun tryParse(id: String): VesselIdentifier? {
            if (id.contains(':')) {
                val ns = id.substringBefore(':')
                val path = id.substringAfter(':', "")
                if (ns.isEmpty() || path.isEmpty()) return null
                return VesselIdentifier(ns, path)
            }
            if (id.isEmpty()) return null
            return VesselIdentifier(null, id)
        }

        fun parse(id: String): VesselIdentifier {
            return tryParse(id) ?: throw ResourceLocationException("Invalid VesselIdentifier")
        }

        fun of(namespace: String, path: String): VesselIdentifier {
            return VesselIdentifier(namespace, path)
        }

        fun vessel(key: String): VesselIdentifier {
            return VesselIdentifier(null, key)
        }

        object StringSerializer : KSerializer<VesselIdentifier> {
            override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
                "com.doofcraft.vessel.Identifier", PrimitiveKind.STRING
            )

            override fun serialize(
                encoder: Encoder, value: VesselIdentifier
            ) {
                encoder.encodeString(value.toString())
            }

            override fun deserialize(decoder: Decoder): VesselIdentifier {
                val str = decoder.decodeString()
                return VesselIdentifier.parse(str)
            }

        }
    }
}