package com.doofcraft.vessel.client.api.render

import com.doofcraft.vessel.common.VesselMod

object VesselItemRenderRegistry {
    private val handlers = mutableMapOf<String, VesselItemRenderHandler>()

    @JvmStatic
    fun register(tagKey: String, handler: VesselItemRenderHandler) {
        val previous = handlers.put(tagKey, handler)
        if (previous != null) {
            VesselMod.LOGGER.warn("Replacing VesselItemRenderHandler for key '{}'", tagKey)
        }
    }

    @JvmStatic
    fun get(tagKey: String): VesselItemRenderHandler? = handlers[tagKey]
}
