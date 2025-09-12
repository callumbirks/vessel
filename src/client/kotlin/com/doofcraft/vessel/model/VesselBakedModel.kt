package com.doofcraft.vessel.model

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random

@Environment(EnvType.CLIENT)
class VesselBakedModel(
    private val fallback: BakedModel,
    private val overrides: List<VesselBakedOverride>,
) : BakedModel {
    override fun getQuads(
        state: BlockState?, face: Direction?, random: Random
    ): List<BakedQuad> = fallback.getQuads(state, face, random)

    override fun useAmbientOcclusion(): Boolean = fallback.useAmbientOcclusion()

    override fun hasDepth(): Boolean = fallback.hasDepth()

    override fun isSideLit(): Boolean = fallback.isSideLit

    override fun isBuiltin(): Boolean = false

    override fun getParticleSprite(): Sprite = fallback.particleSprite

    override fun getTransformation(): ModelTransformation = fallback.transformation

    override fun getOverrides(): ModelOverrideList = object : ModelOverrideList() {
        override fun apply(
            model: BakedModel, stack: ItemStack, world: ClientWorld?, entity: LivingEntity?, seed: Int
        ): BakedModel {
            val override = overrides.firstOrNull { it.predicate.test(stack) }
            return override?.model ?: fallback
        }
    }
}