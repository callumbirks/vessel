package com.doofcraft.vessel.client.mixin;

import com.doofcraft.vessel.client.api.render.VesselItemRenderRegistry;
import com.doofcraft.vessel.client.model.VesselCustomItemBakedModel;
import com.doofcraft.vessel.common.component.VesselTag;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Inject(
        method = "getModel(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Lnet/minecraft/client/resources/model/BakedModel;",
        at = @At("RETURN"),
        cancellable = true
    )
    private void vessel$wrapHandledItemModel(
        ItemStack stack,
        Level level,
        LivingEntity entity,
        int seed,
        CallbackInfoReturnable<BakedModel> cir
    ) {
        VesselTag tag = stack.get(VesselTag.Companion.getCOMPONENT());
        if (tag == null) return;
        if (VesselItemRenderRegistry.get(tag.key) == null) return;

        BakedModel model = cir.getReturnValue();
        if (model == null || model.isCustomRenderer()) return;

        cir.setReturnValue(new VesselCustomItemBakedModel(model));
    }
}
