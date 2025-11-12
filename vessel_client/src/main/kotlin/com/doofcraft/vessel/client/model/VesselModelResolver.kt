package com.doofcraft.vessel.client.model

import com.doofcraft.vessel.client.serialization.vesselSerializationModule
import com.doofcraft.vessel.common.VesselMod
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.model.loading.v1.ModelResolver
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.model.UnbakedModel
import net.minecraft.resources.ResourceLocation

@Environment(EnvType.CLIENT)
object VesselModelResolver: ModelResolver {
    private val json = Json { serializersModule = vesselSerializationModule }

    @OptIn(ExperimentalSerializationApi::class)
    override fun resolveModel(context: ModelResolver.Context): UnbakedModel? {
        if (context.id().namespace != VesselMod.MODID) return null
        VesselMod.LOGGER.info("Resolving model {}", context.id())
        when (context.id().path) {
            "item/item", "block/block", "item/block_item" -> {}
            else -> return null
        }
        val rm = Minecraft.getInstance().resourceManager
        val resId = ResourceLocation.fromNamespaceAndPath(context.id().namespace, "models/${context.id().path}.json")
        val res = rm.getResource(resId).orElse(null) ?: run {
            VesselMod.LOGGER.warn("Failed to get Resource with id $resId")
            return null
        }

        res.open().use { stream ->
            return json.decodeFromStream<VesselUnbakedModel>(stream)
        }
    }
}