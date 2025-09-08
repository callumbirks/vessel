package com.doofcraft.vessel

import com.doofcraft.vessel.model.VesselModelResolver
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin

@Environment(EnvType.CLIENT)
object VesselClient : ClientModInitializer {
	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
        ModelLoadingPlugin.register { ctx ->
            ctx.resolveModel().register(VesselModelResolver)
        }
	}
}