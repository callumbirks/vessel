package com.doofcraft.vessel.model

import com.doofcraft.vessel.VesselMod
import com.squareup.moshi.JsonReader
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.model.loading.v1.ModelResolver
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.util.Identifier
import okio.buffer
import okio.source

@Environment(EnvType.CLIENT)
object VesselModelResolver: ModelResolver {
    override fun resolveModel(context: ModelResolver.Context): UnbakedModel? {
        if (context.id().namespace != VesselMod.MODID) return null
        VesselMod.LOGGER.info("Resolving model {}", context.id())
        when (context.id().path) {
            "item/item", "block/block", "item/block_item" -> {}
            else -> return null
        }
        val rm = MinecraftClient.getInstance().resourceManager
        val resId = Identifier.of(context.id().namespace, "models/${context.id().path}.json")
        val res = rm.getResource(resId).orElse(null) ?: run {
            VesselMod.LOGGER.warn("Failed to get Resource with id $resId")
            return null
        }

        res.inputStream.use { stream ->
            val source = stream.source().buffer()
            val json = JsonReader.of(source)
            return VesselUnbakedModel.Deserializer().fromJson(json)
        }
    }
}