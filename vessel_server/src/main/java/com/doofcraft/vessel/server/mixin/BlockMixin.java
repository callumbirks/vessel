package com.doofcraft.vessel.server.mixin;

import com.doofcraft.vessel.common.base.VesselBaseBlockEntity;
import com.doofcraft.vessel.common.component.VesselTag;
import com.doofcraft.vessel.common.api.item.VesselBlock;
import com.doofcraft.vessel.server.api.VesselRegistry;
import com.doofcraft.vessel.server.api.events.VesselEvents;
import com.doofcraft.vessel.server.api.events.world.BlockEntityLoadEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "setPlacedBy", at = @At("TAIL"))
    private void vessel$setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, CallbackInfo ci) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (!(entity instanceof VesselBaseBlockEntity vesselEntity)) return;

        VesselTag tag = vesselEntity.get(VesselTag.Companion.getCOMPONENT());
        if (tag == null) return;

        VesselBlock block = VesselRegistry.INSTANCE.getBlock(tag.key);
        if (block == null) return;

        block.onPlaced((ServerLevel) level, pos, placer, vesselEntity);
        VesselEvents.BLOCK_ENTITY_LOAD.emit(new BlockEntityLoadEvent(vesselEntity, (ServerLevel) level));
    }
}
