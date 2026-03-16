package com.doofcraft.vessel.client.api.render

import com.doofcraft.vessel.common.VesselMod

object VesselBlockRenderRegistry {
    private val handlers = mutableMapOf<String, VesselBlockRenderHandler>()

    @JvmStatic
    fun register(tagKey: String, handler: VesselBlockRenderHandler) {
        val previous = handlers.put(tagKey, handler)
        if (previous != null) {
            VesselMod.LOGGER.warn("Replacing VesselBlockRenderHandler for key '{}'", tagKey)
        }
    }

    @JvmStatic
    fun get(tagKey: String): VesselBlockRenderHandler? = handlers[tagKey]
}
