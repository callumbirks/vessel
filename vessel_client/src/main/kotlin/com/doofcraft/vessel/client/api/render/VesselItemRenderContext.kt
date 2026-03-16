package com.doofcraft.vessel.client.api.render

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

data class VesselItemRenderContext(
    val stack: ItemStack,
    val displayContext: ItemDisplayContext,
    val poseStack: PoseStack,
    val bufferSource: MultiBufferSource,
    val packedLight: Int,
    val packedOverlay: Int
)
