package com.doofcraft.vessel

import com.doofcraft.vessel.model.VesselBlockEntityRenderer
import com.doofcraft.vessel.model.VesselModelResolver
import com.doofcraft.vessel.registry.ModBlockEntities
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories

@Environment(EnvType.CLIENT)
object VesselClient : ClientModInitializer {
	override fun onInitializeClient() {
        VesselMod.LOGGER.info("Initializing client...")
        ModelLoadingPlugin.register { ctx ->
            VesselMod.LOGGER.info("Registering ModelLoadingPlugin...")
            ctx.resolveModel().register(VesselModelResolver)
        }
        BlockEntityRendererFactories.register(ModBlockEntities.VESSEL) { ctx ->
            VesselBlockEntityRenderer(ctx)
        }
	}
}