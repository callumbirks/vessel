package com.doofcraft.vessel.common.component

import com.mojang.serialization.Codec
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

@Serializable(with = BlockShapeSerializer::class)
data class BlockShapeComponent(
    val xMin: Double, val yMin: Double, val zMin: Double, val xMax: Double, val yMax: Double, val zMax: Double
) {
    companion object {
        val CODEC: Codec<BlockShapeComponent> =
            Codec.DOUBLE.listOf(6, 6).xmap(
                { BlockShapeComponent(it[0], it[1], it[2], it[3], it[4], it[5]) },
                { listOf(it.xMin, it.yMin, it.zMin, it.xMax, it.yMax, it.zMax) })
    }

    // Reject shapes which are too small
    fun isValid(): Boolean {
        return xMax - xMin > 0.05f && yMax - yMin > 0.05f && zMax - zMin > 0.05f
    }

    fun asVoxelShape(): VoxelShape = Shapes.create(xMin, yMin, zMin, xMax, yMax, zMax)
}

object BlockShapeSerializer : KSerializer<BlockShapeComponent> {
    private val listSerializer = ListSerializer(Double.serializer())
    override val descriptor = listSerializer.descriptor

    override fun serialize(
        encoder: Encoder,
        value: BlockShapeComponent
    ) {
        val list = listOf(value.xMin, value.yMin, value.zMin, value.xMax, value.yMax, value.zMax)
        encoder.encodeSerializableValue(listSerializer, list)
    }

    override fun deserialize(decoder: Decoder): BlockShapeComponent {
        val list = decoder.decodeSerializableValue(listSerializer)
        return BlockShapeComponent(list[0], list[1], list[2], list[3], list[4], list[5])
    }
}
