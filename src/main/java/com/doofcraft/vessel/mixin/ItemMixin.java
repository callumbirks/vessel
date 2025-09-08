package com.doofcraft.vessel.mixin;

import com.doofcraft.vessel.api.VesselBlock;
import com.doofcraft.vessel.api.VesselItem;
import com.doofcraft.vessel.api.VesselRegistry;
import com.doofcraft.vessel.base.VesselBaseBlockEntity;
import com.doofcraft.vessel.component.VesselTag;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void vessel$use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);
        VesselTag tag = stack.get(VesselTag.Companion.getCOMPONENT());
        if (tag == null) return;
        VesselItem item = VesselRegistry.INSTANCE.getItem(tag.getKey());
        if (item == null) return;

        TypedActionResult<ItemStack> result = item.use(stack, (ServerWorld) world, (ServerPlayerEntity) user, hand);
        if (result.getResult().isAccepted()) {
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "useOnEntity", at = @At("HEAD"), cancellable = true)
    private void vessel$useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        VesselTag tag = stack.get(VesselTag.Companion.getCOMPONENT());
        if (tag == null) return;
        VesselItem item = VesselRegistry.INSTANCE.getItem(tag.getKey());
        if (item == null) return;

        ActionResult result = item.useOnEntity(stack, (ServerPlayerEntity) user, entity, hand);
        if (result.isAccepted()) {
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "onClicked", at = @At("HEAD"), cancellable = true)
    private void vessel$onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference, CallbackInfoReturnable<Boolean> cir) {
        VesselTag tag = stack.get(VesselTag.Companion.getCOMPONENT());
        if (tag == null) return;
        VesselItem item = VesselRegistry.INSTANCE.getItem(tag.getKey());
        if (item == null) return;

        ActionResult result = item.onClicked(stack, otherStack, (ServerPlayerEntity) player);
        if (result.isAccepted()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "finishUsing", at = @At("HEAD"), cancellable = true)
    private void vessel$finishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        VesselTag tag = stack.get(VesselTag.Companion.getCOMPONENT());
        if (tag == null) return;
        VesselItem item = VesselRegistry.INSTANCE.getItem(tag.getKey());
        if (item == null) return;

        TypedActionResult<ItemStack> result = item.finishUsing(stack, (ServerWorld)world, user);
        if (result.getResult().isAccepted()) {
            cir.setReturnValue(result.getValue());
        }
    }
}
