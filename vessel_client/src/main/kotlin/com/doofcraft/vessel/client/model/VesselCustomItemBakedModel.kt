package com.doofcraft.vessel.client.model

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.state.BlockState

@Environment(EnvType.CLIENT)
class VesselCustomItemBakedModel(
    private val delegate: BakedModel,
) : BakedModel {
    override fun getQuads(state: BlockState?, direction: Direction?, random: RandomSource): List<BakedQuad> {
        return delegate.getQuads(state, direction, random)
    }

    override fun useAmbientOcclusion(): Boolean = delegate.useAmbientOcclusion()

    override fun isGui3d(): Boolean = delegate.isGui3d

    override fun usesBlockLight(): Boolean = delegate.usesBlockLight()

    override fun isCustomRenderer(): Boolean = true

    override fun getParticleIcon(): TextureAtlasSprite = delegate.particleIcon

    override fun getTransforms(): ItemTransforms = delegate.transforms

    override fun getOverrides(): ItemOverrides = delegate.overrides
}
