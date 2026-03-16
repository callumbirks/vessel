package com.doofcraft.vessel.client.api.render

fun interface VesselItemRenderHandler {
    /**
     * Called after vanilla item transforms for [VesselItemRenderContext.model] have already been applied.
     * Return true to consume rendering entirely, or false to let vanilla render the resolved model.
     */
    fun render(context: VesselItemRenderContext): Boolean
}
