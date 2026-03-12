package com.doofcraft.vessel.common.component

import com.mojang.serialization.Codec

data class BlockDoesDropComponent(val doesDrop: Boolean) {
    companion object {
        val CODEC: Codec<BlockDoesDropComponent> = Codec.BOOL.xmap({ BlockDoesDropComponent(it) }, { it.doesDrop })
    }
}