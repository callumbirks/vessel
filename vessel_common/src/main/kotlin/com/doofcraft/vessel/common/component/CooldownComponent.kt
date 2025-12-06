package com.doofcraft.vessel.common.component

import com.mojang.serialization.Codec

data class CooldownComponent(val endTick: Int) {
    companion object {
        val CODEC: Codec<CooldownComponent> = Codec.INT.xmap({ CooldownComponent(it) }, { it.endTick })
    }
}
