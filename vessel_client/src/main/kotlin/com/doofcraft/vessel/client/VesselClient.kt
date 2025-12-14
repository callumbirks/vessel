package com.doofcraft.vessel.client

import com.doofcraft.vessel.client.model.VesselBlockEntityRenderer
import com.doofcraft.vessel.client.model.VesselModelResolver
import com.doofcraft.vessel.common.VesselMod
import com.doofcraft.vessel.common.api.VesselBehaviourRegistry
import com.doofcraft.vessel.common.registry.BehaviourComponents
import com.doofcraft.vessel.common.registry.ModBlockEntities
import com.doofcraft.vessel.common.registry.ModBlocks
import com.doofcraft.vessel.common.registry.StackComponents
import com.doofcraft.vessel.common.registry.ModItems
import com.doofcraft.vessel.common.registry.VesselPackets
import com.doofcraft.vessel.common.tooltip.TooltipRegistry
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers

@Environment(EnvType.CLIENT)
object VesselClient : ClientModInitializer {
	override fun onInitializeClient() {
        VesselMod.LOGGER.info("Initializing client...")
        VesselMod.preInitialize()
        StackComponents.register()
        BehaviourComponents.register()
        ModItems.register()
        ModBlocks.register()
        ModBlockEntities.register()
        VesselPackets.register()
        TooltipRegistry.registerClient()
        VesselBehaviourRegistry.registerClient()
        ModelLoadingPlugin.register { ctx ->
            VesselMod.LOGGER.info("Registering ModelLoadingPlugin...")
            ctx.resolveModel().register(VesselModelResolver)
        }
        BlockEntityRenderers.register(ModBlockEntities.VESSEL) { ctx ->
            VesselBlockEntityRenderer(ctx)
        }
        ClientLifecycleEvents.CLIENT_STARTED.register { client ->
            VesselMod.initialize(server = null)
        }
	}
}