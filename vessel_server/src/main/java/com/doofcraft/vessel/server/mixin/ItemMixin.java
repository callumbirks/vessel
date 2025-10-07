package com.doofcraft.vessel.server.mixin;

import com.doofcraft.vessel.server.api.VesselRegistry;
import com.doofcraft.vessel.common.api.item.VesselItem;
import com.doofcraft.vessel.common.component.VesselTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void vessel$use(Level level, Player player, InteractionHand usedHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(usedHand);
        VesselTag tag = stack.get(VesselTag.Companion.getCOMPONENT());
        if (tag == null) return;
        VesselItem item = VesselRegistry.INSTANCE.getItem(tag.key);
        if (item == null) return;

        InteractionResultHolder<ItemStack> result = item.use(stack, (ServerLevel) level, (ServerPlayer) player, usedHand);
        if (result.getResult().consumesAction()) {
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "interactLivingEntity", at = @At("HEAD"), cancellable = true)
    private void vessel$useOnEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand, CallbackInfoReturnable<InteractionResult> cir) {
        VesselTag tag = stack.get(VesselTag.Companion.getCOMPONENT());
        if (tag == null) return;
        VesselItem item = VesselRegistry.INSTANCE.getItem(tag.key);
        if (item == null) return;

        InteractionResult result = item.useOnEntity(stack, (ServerPlayer) player, interactionTarget, usedHand);
        if (result.consumesAction()) {
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    private void vessel$onClicked(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
        VesselTag tag = stack.get(VesselTag.Companion.getCOMPONENT());
        if (tag == null) return;
        VesselItem item = VesselRegistry.INSTANCE.getItem(tag.key);
        if (item == null) return;

        InteractionResult result = item.onClicked(stack, other, (ServerPlayer) player);
        if (result.consumesAction()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    private void vessel$finishUsing(ItemStack stack, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        VesselTag tag = stack.get(VesselTag.Companion.getCOMPONENT());
        if (tag == null) return;
        VesselItem item = VesselRegistry.INSTANCE.getItem(tag.key);
        if (item == null) return;

        InteractionResultHolder<ItemStack> result = item.finishUsing(stack, (ServerLevel) level, livingEntity);
        if (result.getResult().consumesAction()) {
            cir.setReturnValue(result.getObject());
        }
    }
}
