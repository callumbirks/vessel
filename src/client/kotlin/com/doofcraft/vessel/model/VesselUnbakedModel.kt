package com.doofcraft.vessel.model

import com.doofcraft.vessel.VesselMod
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.model.*
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.util.Identifier
import java.util.*
import java.util.function.Function

@Environment(EnvType.CLIENT)
@JsonClass(generateAdapter = false)
class VesselUnbakedModel(
    private var parentId: Identifier,
    private val overrides: List<VesselUnbakedOverride>,
) : UnbakedModel {
    private lateinit var parent: UnbakedModel

    override fun getModelDependencies(): Collection<Identifier> = buildList {
        add(parentId)
        overrides.forEach { add(it.model) }
    }

    override fun setParents(modelLoader: Function<Identifier, UnbakedModel>) {
        val existingParents = mutableSetOf<UnbakedModel>()

        existingParents.add(this)
        var parentModel: UnbakedModel? = modelLoader.apply(this.parentId)
        if (existingParents.contains(parentModel)) {
            VesselMod.LOGGER.warn(
                "Found 'parent' loop while loading model '{}' in chain: {} -> {}", this, existingParents.joinToString(
                    " -> ", transform = UnbakedModel::toString
                ), parentId
            )
            parentModel = null
        }
        if (parentModel == null) {
            this.parentId = ModelLoader.MISSING_ID
            parentModel = modelLoader.apply(this.parentId)
        }

        if (parentModel !is JsonUnbakedModel && parentModel !is VesselUnbakedModel) {
            throw IllegalStateException("Vessel BlockModel parent has to be a block model.")
        }

        this.parent = parentModel

        parentModel.setParents(modelLoader)

        this.overrides.forEach { override ->
            val model = modelLoader.apply(override.model)
            if (!Objects.equals(model, this)) {
                model.setParents(modelLoader)
            }
        }
    }

    override fun bake(
        baker: Baker, textureGetter: Function<SpriteIdentifier, Sprite>, rotationContainer: ModelBakeSettings
    ): BakedModel {
        val bakedParent =
            parent.bake(baker, textureGetter, rotationContainer)
                ?: throw IllegalStateException("VesselBakedModel must have a parent")
        if (bakedParent is VesselBakedModel) return bakedParent
        val bakedMap = overrides.map { VesselBakedOverride(it.predicate, baker.bake(it.model, rotationContainer)!!) }
        return VesselBakedModel(bakedParent, bakedMap)
    }

    class Deserializer {
        @FromJson
        fun fromJson(reader: JsonReader): VesselUnbakedModel {
            reader.beginObject()
            var parent: Identifier? = null
            var overrides = listOf<VesselUnbakedOverride>()
            while (reader.hasNext()) {
                val name = reader.nextName()
                when (name) {
                    "parent" -> parent = Identifier.of(reader.nextString())
                    "vessel_overrides" -> overrides = readOverrides(reader)
                    else -> reader.skipValue()
                }
            }
            if (parent == null) throw JsonDataException("'parent' is missing")
            return VesselUnbakedModel(parent, overrides)
        }

        private fun readOverrides(reader: JsonReader): List<VesselUnbakedOverride> {
            val list = mutableListOf<VesselUnbakedOverride>()
            reader.beginArray()
            while (reader.hasNext()) {
                var predicate: VesselPredicate? = null
                var model: Identifier? = null
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "when" -> predicate = VesselPredicate.Deserializer().fromJson(reader)
                        "model" -> model = Identifier.of(reader.nextString())
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                if (predicate == null) throw JsonDataException("'when' clause is missing from override at  ${reader.path}")
                if (model == null) throw JsonDataException("'model' is missing from override at ${reader.path}")
                list.add(VesselUnbakedOverride(predicate, model))
            }
            reader.endArray()
            return list
        }
    }
}