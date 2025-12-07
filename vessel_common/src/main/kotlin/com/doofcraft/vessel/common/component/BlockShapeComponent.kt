package com.doofcraft.vessel.common.component

import com.mojang.serialization.Codec
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.math.abs

@Serializable(with = BlockShapeSerializer::class)
data class BlockShapeComponent(
    val x1: Double, val y1: Double, val z1: Double, val x2: Double, val y2: Double, val z2: Double
) {
    companion object {
        val CODEC: Codec<BlockShapeComponent> =
            Codec.DOUBLE.listOf(6, 6).xmap(
                { BlockShapeComponent(it[0], it[1], it[2], it[3], it[4], it[5]) },
                { listOf(it.x1, it.y1, it.z1, it.x2, it.y2, it.z2) })
    }

    // Reject shapes which are too small.
    fun isValid(): Boolean {
        return abs(x2 - x1) > 0.05f && abs(y2 - y1) > 0.05f && abs(z2 - z1) > 0.05f
    }
}

object BlockShapeSerializer : KSerializer<BlockShapeComponent> {
    private val listSerializer = ListSerializer(Double.serializer())
    override val descriptor = listSerializer.descriptor

    override fun serialize(
        encoder: Encoder,
        value: BlockShapeComponent
    ) {
        val list = listOf(value.x1, value.y1, value.z1, value.x2, value.y2, value.z2)
        encoder.encodeSerializableValue(listSerializer, list)
    }

    override fun deserialize(decoder: Decoder): BlockShapeComponent {
        val list = decoder.decodeSerializableValue(listSerializer)
        return BlockShapeComponent(list[0], list[1], list[2], list[3], list[4], list[5])
    }
}
