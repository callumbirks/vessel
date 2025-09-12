package com.doofcraft.vessel.model

import com.doofcraft.vessel.VesselMod
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.Baker
import net.minecraft.client.render.model.ModelBakeSettings
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import java.io.Reader
import java.lang.reflect.Type
import java.util.function.Function

@Environment(EnvType.CLIENT)
class VesselUnbakedModel(
    private val parent: Identifier,
    private val overrides: List<VesselUnbakedOverride>,
) : UnbakedModel {

    override fun getModelDependencies(): Collection<Identifier> = buildList {
        add(parent)
        overrides.forEach { add(it.model) }
    }

    override fun setParents(modelLoader: Function<Identifier, UnbakedModel>) {
        // NO-OP: We don't allow set parents for Vessel Models.
        return
    }

    override fun bake(
        baker: Baker, textureGetter: Function<SpriteIdentifier, Sprite>, rotationContainer: ModelBakeSettings
    ): BakedModel {
        val parent = baker.bake(parent, rotationContainer)!!
        val bakedMap = overrides.map { VesselBakedOverride(it.predicate, baker.bake(it.model, rotationContainer)!!) }
        return VesselBakedModel(parent, bakedMap)
    }

    companion object {
        val GSON: Gson =
            GsonBuilder().registerTypeAdapter(VesselUnbakedModel::class.java, Deserializer()).registerTypeAdapter(
                VesselPredicate::class.java, VesselPredicate.Deserializer()
            ).create()

        fun deserialize(input: Reader): VesselUnbakedModel {
            return JsonHelper.deserialize(GSON, input, VesselUnbakedModel::class.java)
        }
    }

    class Deserializer : JsonDeserializer<VesselUnbakedModel> {
        override fun deserialize(
            json: JsonElement, typeOfT: Type, context: JsonDeserializationContext
        ): VesselUnbakedModel {
            val root = json.asJsonObject
            val overrides = overridesFromJson(root, context)
            val parent = parentFromJson(root)
            return VesselUnbakedModel(parent, overrides)
        }

        private fun overridesFromJson(
            obj: JsonObject, context: JsonDeserializationContext
        ): List<VesselUnbakedOverride> {
            return obj.getAsJsonArray("vessel_overrides")?.map {
                val override = it.asJsonObject
                val predicate: VesselPredicate = context.deserialize(override.get("when"), VesselPredicate::class.java)
                val modelStr = override.get("model").asString
                val model =
                    if (modelStr.contains(':')) Identifier.of(modelStr) else Identifier.of(VesselMod.MODID, modelStr)
                VesselUnbakedOverride(predicate, model)
            } ?: emptyList()
        }

        private fun parentFromJson(obj: JsonObject): Identifier {
            return Identifier.of(JsonHelper.getString(obj, "parent"))
        }
    }
}