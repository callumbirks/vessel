package com.doofcraft.vessel.client

import com.doofcraft.vessel.client.model.VesselBlockEntityRenderer
import com.doofcraft.vessel.client.model.VesselModelResolver
import com.doofcraft.vessel.common.registry.ModBlockEntities
import com.doofcraft.vessel.common.registry.ModBlocks
import com.doofcraft.vessel.common.registry.ModComponents
import com.doofcraft.vessel.common.registry.ModItems
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers

@Environment(EnvType.CLIENT)
object VesselClient : ClientModInitializer {
	override fun onInitializeClient() {
        VesselMod.LOGGER.info("Initializing client...")
        ModComponents.register()
        ModItems.register()
        ModBlocks.register()
        ModBlockEntities.register()
        ModelLoadingPlugin.register { ctx ->
            VesselMod.LOGGER.info("Registering ModelLoadingPlugin...")
            ctx.resolveModel().register(VesselModelResolver)
        }
        BlockEntityRenderers.register(ModBlockEntities.VESSEL) { ctx ->
            VesselBlockEntityRenderer(ctx)
        }
	}
}