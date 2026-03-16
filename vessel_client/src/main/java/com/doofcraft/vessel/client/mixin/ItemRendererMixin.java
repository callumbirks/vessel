package com.doofcraft.vessel.client.mixin;

import com.doofcraft.vessel.client.api.render.VesselItemRenderContext;
import com.doofcraft.vessel.client.api.render.VesselItemRenderHandler;
import com.doofcraft.vessel.client.api.render.VesselItemRenderRegistry;
import com.doofcraft.vessel.common.VesselMod;
import com.doofcraft.vessel.common.component.VesselTag;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Inject(
        method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
        at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/resources/model/BakedModel;isCustomRenderer()Z",
                shift = At.Shift.BEFORE
        ),
        cancellable = true
    )
    private void vessel$renderHandledItem(
        ItemStack stack,
        ItemDisplayContext displayContext,
        boolean leftHanded,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int packedLight,
        int packedOverlay,
        BakedModel model,
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
                packedOverlay,
                model,
                leftHanded
            )
        );
        if (!handled) return;

        poseStack.popPose();
        ci.cancel();
    }
}
