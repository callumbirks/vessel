package com.doofcraft.vessel.client.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.block.model.BlockModel
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.resources.model.Material
import net.minecraft.client.resources.model.ModelBaker
import net.minecraft.client.resources.model.ModelBakery
import net.minecraft.client.resources.model.ModelState
import net.minecraft.client.resources.model.UnbakedModel
import net.minecraft.resources.ResourceLocation
import java.util.*
import java.util.function.Function

@Environment(EnvType.CLIENT)
@JsonClass(generateAdapter = false)
class VesselUnbakedModel(
    private var parentId: ResourceLocation,
    private val overrides: List<VesselUnbakedOverride>,
) : UnbakedModel {
    private lateinit var parent: UnbakedModel

    override fun getDependencies(): Collection<ResourceLocation> = buildList {
        add(parentId)
        overrides.forEach { add(it.model) }
    }

    override fun resolveParents(resolver: Function<ResourceLocation, UnbakedModel>) {
        val existingParents = mutableSetOf<UnbakedModel>()

        existingParents.add(this)
        var parentModel: UnbakedModel? = resolver.apply(this.parentId)
        if (existingParents.contains(parentModel)) {
            VesselMod.LOGGER.warn(
                "Found 'parent' loop while loading model '{}' in chain: {} -> {}", this, existingParents.joinToString(
                    " -> ", transform = UnbakedModel::toString
                ), parentId
            )
            parentModel = null
        }
        if (parentModel == null) {
            this.parentId = ModelBakery.MISSING_MODEL_LOCATION
            parentModel = resolver.apply(this.parentId)
        }

        if (parentModel !is BlockModel && parentModel !is VesselUnbakedModel) {
            throw IllegalStateException("Vessel BlockModel parent has to be a block model.")
        }

        this.parent = parentModel

        parentModel.resolveParents(resolver)

        this.overrides.forEach { override ->
            val model = resolver.apply(override.model)
            if (!Objects.equals(model, this)) {
                model.resolveParents(resolver)
            }
        }
    }

    override fun bake(
        baker: ModelBaker,
        spriteGetter: Function<Material, TextureAtlasSprite>,
        state: ModelState
    ): BakedModel {
        val bakedParent =
            parent.bake(baker, spriteGetter, state)
                ?: throw IllegalStateException("VesselBakedModel must have a parent")
        if (bakedParent is VesselBakedModel) return bakedParent
        val bakedMap = overrides.map { VesselBakedOverride(it.predicate, baker.bake(it.model, state)!!) }
        return VesselBakedModel(bakedParent, bakedMap)
    }

    class Deserializer {
        @FromJson
        fun fromJson(reader: JsonReader): VesselUnbakedModel {
            reader.beginObject()
            var parent: ResourceLocation? = null
            var overrides = listOf<VesselUnbakedOverride>()
            while (reader.hasNext()) {
                val name = reader.nextName()
                when (name) {
                    "parent" -> parent = ResourceLocation.parse(reader.nextString())
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
                var model: ResourceLocation? = null
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "when" -> predicate = VesselPredicate.Deserializer().fromJson(reader)
                        "model" -> model = ResourceLocation.parse(reader.nextString())
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