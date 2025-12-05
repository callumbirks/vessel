@file:UseSerializers(ResourceLocationSerializer::class)

package com.doofcraft.vessel.client.model

import com.doofcraft.vessel.common.VesselMod
import com.doofcraft.vessel.common.serialization.adapters.ResourceLocationSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.block.model.BlockModel
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.*
import net.minecraft.resources.ResourceLocation
import java.util.*
import java.util.function.Function

@Environment(EnvType.CLIENT)
@Serializable
class VesselUnbakedModel(
    @SerialName("parent") var parentId: ResourceLocation,
    @SerialName("vessel_overrides") val overrides: List<VesselUnbakedOverride>,
) : UnbakedModel {
    @Transient
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
}