package com.doofcraft.vessel.mixin;

import com.doofcraft.vessel.api.events.VesselEvents;
import com.doofcraft.vessel.base.VesselBaseBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {
    @Inject(method = "setBlockEntity", at = @At("TAIL"))
    private void vessel$afterBlockEntityLoaded(BlockEntity blockEntity, CallbackInfo ci) {
        if (!(blockEntity instanceof VesselBaseBlockEntity vessel)) return;
        // Don't bother emitting event if the BE isn't initialized, we can do it later.
        if (!vessel.isInitialized()) return;
        Level level = vessel.getLevel();
        if (level instanceof ServerLevel) {
            VesselEvents.BLOCK_ENTITY_LOAD.invoker().onLoad(vessel, (ServerLevel) level);
        }
    }
}
