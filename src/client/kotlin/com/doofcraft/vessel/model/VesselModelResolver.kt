package com.doofcraft.vessel.model

import com.doofcraft.vessel.VesselMod
import com.google.gson.JsonParser
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.model.loading.v1.ModelResolver
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.UnbakedModel

@Environment(EnvType.CLIENT)
object VesselModelResolver: ModelResolver {
    override fun resolveModel(context: ModelResolver.Context): UnbakedModel? {
        if (context.id().namespace != VesselMod.MODID) return null
        val rm = MinecraftClient.getInstance().resourceManager
        val res = rm.getResource(context.id()).orElse(null) ?: return null

        res.reader.use { reader ->
            val json = JsonParser.parseReader(reader).asJsonObject
            val loader = json.getAsJsonPrimitive("loader")?.asString ?: return null
            if (loader != ID.toString()) return null

            return VesselUnbakedModel.deserialize(reader)
        }
    }

    val ID = VesselMod.vesselResource("model")
}