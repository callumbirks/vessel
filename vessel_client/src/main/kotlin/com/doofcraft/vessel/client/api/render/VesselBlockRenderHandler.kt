package com.doofcraft.vessel.client.api.render

fun interface VesselBlockRenderHandler {
    fun render(context: VesselBlockRenderContext): Boolean
}
