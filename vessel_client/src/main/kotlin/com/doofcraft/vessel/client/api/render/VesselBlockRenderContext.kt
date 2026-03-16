package com.doofcraft.vessel.client.api.render

import com.doofcraft.vessel.common.base.VesselBaseBlockEntity
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.world.item.ItemStack

data class VesselBlockRenderContext(
    val blockEntity: VesselBaseBlockEntity,
    val partialTick: Float,
    val poseStack: PoseStack,
    val bufferSource: MultiBufferSource,
    val packedLight: Int,
    val packedOverlay: Int,
    val itemStack: ItemStack,
    val yaw: Float
)
