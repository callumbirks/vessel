package com.doofcraft.vessel.server

import com.doofcraft.vessel.common.VesselMod
import com.doofcraft.vessel.common.api.VesselEvents
import com.doofcraft.vessel.common.registry.*
import com.doofcraft.vessel.server.api.VesselRegistry
import com.doofcraft.vessel.server.api.async.VesselAsync
import com.doofcraft.vessel.server.api.config.VesselConfigRegistry
import com.doofcraft.vessel.server.api.events.VesselServerEvents
import com.doofcraft.vessel.server.api.redis.RedisThreadedConnection
import com.doofcraft.vessel.server.tooltip.TooltipConfig
import com.doofcraft.vessel.server.ui.UiManager
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer

object VesselServer: DedicatedServerModInitializer {
    lateinit var server: MinecraftServer
        private set

    override fun onInitializeServer() {
        VesselMod.preInitialize()

        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            this.server = server
            VesselMod.initialize(server)
            lateInitialize()
        }

        ServerLifecycleEvents.SERVER_STOPPED.register { server ->
            VesselAsync.shutdown()
            RedisThreadedConnection.shutdown()
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            ModCommands.register(dispatcher)
        }

        StackComponents.register()
        BehaviourComponents.register()
        ModItems.register()
        ModBlocks.register()
        ModBlockEntities.register()
        VesselPackets.register()
        UiManager.register(VesselAsync.Scope)
        VesselDataProvider.registerDefaults()
        VesselServerEvents.register()

        VesselConfigRegistry.register(VesselConfig)
        VesselConfigRegistry.register(TooltipConfig)
    }

    // Stuff that needs to run after all mods have been loaded and initialized.
    private fun lateInitialize() {
        VesselDataProvider.reloadAll()
        VesselRegistry.registerAllBehaviours()
    }
}