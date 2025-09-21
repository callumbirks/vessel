package com.doofcraft.vessel.model

import com.doofcraft.vessel.base.VesselBaseBlockEntity
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.item.ItemDisplayContext

class VesselBlockEntityRenderer(context: BlockEntityRendererProvider.Context) :
    BlockEntityRenderer<VesselBaseBlockEntity> {
    private val itemRenderer = Minecraft.getInstance().itemRenderer

    override fun render(
        blockEntity: VesselBaseBlockEntity,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    ) {
        val stack = blockEntity.item
        if (stack.isEmpty || blockEntity.level == null) return

        poseStack.pushPose()
        poseStack.translate(0.5, 0.0, 0.5)

        poseStack.mulPose(Axis.YN.rotationDegrees(blockEntity.yaw))

        val model = itemRenderer.getModel(stack, blockEntity.level, null, blockEntity.blockPos.asLong().toInt())
        val packedLight = LevelRenderer.getLightColor(blockEntity.level!!, blockEntity.blockState, blockEntity.blockPos)

        itemRenderer.render(
            stack, ItemDisplayContext.GROUND, false, poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, model
        )

        poseStack.popPose()
    }
}