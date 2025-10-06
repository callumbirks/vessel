package com.doofcraft.vessel

import com.doofcraft.vessel.registry.ModBlockEntities
import com.doofcraft.vessel.registry.ModBlocks
import com.doofcraft.vessel.registry.ModComponents
import com.doofcraft.vessel.registry.ModItems
import net.fabricmc.api.EnvType
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
import org.slf4j.LoggerFactory

object VesselMod : ModInitializer {
    const val MODID = "vessel"
    @JvmField
    val LOGGER = LoggerFactory.getLogger(MODID)

    lateinit var environment: EnvType
    val isDedicatedServer: Boolean get() = environment == EnvType.SERVER

    override fun onInitialize() {
        LOGGER.info("Initializing...")
        environment = FabricLoader.getInstance().environmentType
        ModComponents.register()
        ModItems.register()
        ModBlocks.register()
        ModBlockEntities.register()
    }
}