package com.doofcraft.vessel

import com.doofcraft.vessel.registry.ModBlockEntities
import com.doofcraft.vessel.registry.ModBlocks
import com.doofcraft.vessel.registry.ModComponents
import com.doofcraft.vessel.registry.ModItems
import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

object VesselMod : ModInitializer {
    const val MODID = "vessel"
    val LOGGER = LoggerFactory.getLogger(MODID)

    fun vesselResource(name: String): Identifier {
        return Identifier.of(MODID, name)
    }

    override fun onInitialize() {
        LOGGER.info("Initializing...")
        ModComponents.register()
        ModItems.register()
        ModBlocks.register()
        ModBlockEntities.register()
    }
}