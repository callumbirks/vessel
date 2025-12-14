package com.doofcraft.vessel.common

import com.doofcraft.vessel.common.api.VesselEvents
import net.minecraft.server.MinecraftServer
import org.slf4j.LoggerFactory

object VesselMod {
    const val MODID = "vessel"

    @JvmField
    val LOGGER = LoggerFactory.getLogger(MODID)

    var server: MinecraftServer? = null
        private set

    var isInitialized = false
        private set

    val isServer
        get() = server != null

    val isClient
        get() = server == null

    fun preInitialize() {
        VesselEvents.register()
    }

    fun initialize(server: MinecraftServer?) {
        require(!this.isInitialized) { "VesselMod has been initialized more than once!" }
        this.server = server
        this.isInitialized = true
    }
}