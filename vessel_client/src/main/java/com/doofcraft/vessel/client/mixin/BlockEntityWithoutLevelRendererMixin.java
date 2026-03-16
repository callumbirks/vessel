package com.doofcraft.vessel.client.mixin;

import com.doofcraft.vessel.client.api.render.VesselItemRenderContext;
import com.doofcraft.vessel.client.api.render.VesselItemRenderHandler;
import com.doofcraft.vessel.client.api.render.VesselItemRenderRegistry;
import com.doofcraft.vessel.common.component.VesselTag;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityWithoutLevelRenderer.class)
public class BlockEntityWithoutLevelRendererMixin {
    @Inject(method = "renderByItem", at = @At("HEAD"), cancellable = true)
    private void vessel$renderCustomVesselItem(
        ItemStack stack,
        ItemDisplayContext displayContext,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int packedLight,
        int packedOverlay,
        CallbackInfo ci
    ) {
        VesselTag tag = stack.get(VesselTag.Companion.getCOMPONENT());
        if (tag == null) return;

        VesselItemRenderHandler handler = VesselItemRenderRegistry.get(tag.key);
        if (handler == null) return;

        boolean handled = handler.render(
            new VesselItemRenderContext(
                stack,
                displayContext,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay
            )
        );
        if (handled) {
            ci.cancel();
        }
    }
}
