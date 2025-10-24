package com.doofcraft.vessel.server.mixin;

import com.doofcraft.vessel.common.api.item.VesselBlock;
import com.doofcraft.vessel.common.base.VesselBaseBlockEntity;
import com.doofcraft.vessel.common.component.VesselTag;
import com.doofcraft.vessel.server.api.VesselRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
    @Inject(method = "onRemove", at = @At("HEAD"))
    void vessel$onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston, CallbackInfo ci) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (!(entity instanceof VesselBaseBlockEntity vesselEntity)) return;

        VesselTag tag = vesselEntity.get(VesselTag.Companion.getCOMPONENT());
        if (tag == null) return;

        VesselBlock block = VesselRegistry.getBlock(tag.key);
        if (block == null) return;

        block.onDestroyed(level, pos, vesselEntity);
    }
}
