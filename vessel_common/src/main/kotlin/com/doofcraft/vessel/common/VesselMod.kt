package com.doofcraft.vessel.common

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

    fun setServer(server: MinecraftServer) {
        require(this.server == null) { "VesselMod.server has been set more than once!" }
        this.server = server
    }

    fun setInitialized() {
        require(!this.isInitialized) { "VesselMod.isInitialized has been set more than once!" }
        this.isInitialized = true
    }
}