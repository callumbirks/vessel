package com.doofcraft.vessel.client.model

import com.doofcraft.vessel.client.api.render.VesselBlockRenderContext
import com.doofcraft.vessel.client.api.render.VesselBlockRenderRegistry
import com.doofcraft.vessel.common.base.VesselBaseBlockEntity
import com.doofcraft.vessel.common.component.VesselTag
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
        val level = blockEntity.level ?: return
        if (stack.isEmpty) return

        poseStack.pushPose()
        poseStack.translate(0.5, 0.0, 0.5)

        poseStack.mulPose(Axis.YN.rotationDegrees(blockEntity.yaw))

        val resolvedPackedLight = LevelRenderer.getLightColor(level, blockEntity.blockState, blockEntity.blockPos)
        val handled = stack.get(VesselTag.COMPONENT)?.key?.let { key ->
            VesselBlockRenderRegistry.get(key)?.render(
                VesselBlockRenderContext(
                    blockEntity = blockEntity,
                    partialTick = partialTick,
                    poseStack = poseStack,
                    bufferSource = bufferSource,
                    packedLight = resolvedPackedLight,
                    packedOverlay = packedOverlay,
                    itemStack = stack,
                    yaw = blockEntity.yaw
                )
            )
        } == true

        if (!handled) {
            val model = itemRenderer.getModel(stack, level, null, blockEntity.blockPos.asLong().toInt())

            itemRenderer.render(
                stack,
                ItemDisplayContext.HEAD,
                false,
                poseStack,
                bufferSource,
                resolvedPackedLight,
                OverlayTexture.NO_OVERLAY,
                model
            )
        }

        poseStack.popPose()
    }
}
