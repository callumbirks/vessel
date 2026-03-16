package com.doofcraft.vessel.client.api.render

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

/**
 * Invoked after vanilla has already applied the resolved model transform and item anchor translation
 * for the current [displayContext]. Handlers should treat [poseStack] as ready to render, and must
 * leave it balanced relative to entry whether they handle the render or fall through.
 */
data class VesselItemRenderContext(
    val stack: ItemStack,
    val displayContext: ItemDisplayContext,
    val poseStack: PoseStack,
    val bufferSource: MultiBufferSource,
    val packedLight: Int,
    val packedOverlay: Int,
    val model: BakedModel,
    val leftHanded: Boolean
)
