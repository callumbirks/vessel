package com.doofcraft.vessel.model

import com.doofcraft.vessel.base.VesselBaseBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.RotationAxis

class VesselBlockEntityRenderer(context: BlockEntityRendererFactory.Context) :
    BlockEntityRenderer<VesselBaseBlockEntity> {
    private val itemRenderer = MinecraftClient.getInstance().itemRenderer

    override fun render(
        entity: VesselBaseBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        val stack = entity.item
        if (stack.isEmpty) return

        matrices.push()
        matrices.translate(0.5, 0.0, 0.5)

        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(entity.yaw))

        val mode = ModelTransformationMode.GROUND
        val model = itemRenderer.getModel(stack, entity.world, null, entity.pos.asLong().toInt())
        val packedLight = WorldRenderer.getLightmapCoordinates(entity.world, entity.pos)

        itemRenderer.renderItem(
            stack, mode, false, matrices, vertexConsumers, packedLight, OverlayTexture.DEFAULT_UV, model
        )

        matrices.pop()
    }
}