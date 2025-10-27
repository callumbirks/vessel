package com.doofcraft.vessel.common.component

import com.mojang.serialization.Codec

data class BlockShapeComponent(
    val x1: Double, val y1: Double, val z1: Double, val x2: Double, val y2: Double, val z2: Double
) {
    companion object {
        val CODEC: Codec<BlockShapeComponent> =
            Codec.DOUBLE.listOf(6, 6).xmap(
                    { BlockShapeComponent(it[0], it[1], it[2], it[3], it[4], it[5]) },
                    { listOf(it.x1, it.y1, it.z1, it.x2, it.y2, it.z2) })
    }
}
