package com.doofcraft.vessel.mixin;

import com.doofcraft.vessel.api.VesselEvents;
import com.doofcraft.vessel.base.VesselBaseBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {
    @Shadow
    @Nullable
    protected Level level;

    @Inject(method = "loadWithComponents", at = @At("TAIL"))
    private void vessel$afterBlockEntityLoaded(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if ((Object)this instanceof VesselBaseBlockEntity) {
            if (this.level instanceof ServerLevel && this.level != null) {
                VesselEvents.BLOCK_ENTITY_LOAD.invoker().onLoad((VesselBaseBlockEntity)(Object)this, (ServerLevel) level);
            }
        }
    }
}
