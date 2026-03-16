package com.doofcraft.vessel.client.api.render

fun interface VesselItemRenderHandler {
    fun render(context: VesselItemRenderContext): Boolean
}
