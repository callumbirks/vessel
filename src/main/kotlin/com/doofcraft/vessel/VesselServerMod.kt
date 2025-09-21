package com.doofcraft.vessel

import com.doofcraft.vessel.api.VesselRegistry
import com.doofcraft.vessel.base.VesselBaseBlockEntity
import com.doofcraft.vessel.component.VesselTag
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult

object VesselServerMod: DedicatedServerModInitializer {
    override fun onInitializeServer() {
        UseBlockCallback.EVENT.register(::useBlock)
    }

    fun useBlock(player: Player, world: Level, hand: InteractionHand, blockHitResult: BlockHitResult): InteractionResult {
        if (player.isSpectator || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS
        val blockEntity = world.getBlockEntity(blockHitResult.blockPos)
        if (blockEntity == null || blockEntity !is VesselBaseBlockEntity) return InteractionResult.PASS
        val tag = blockEntity.item.get(VesselTag.COMPONENT)
            ?: return InteractionResult.PASS
        val block = VesselRegistry.getBlock(tag.key)
            ?: return InteractionResult.PASS
        return block.use(world as ServerLevel, blockEntity, player as ServerPlayer, hand)
    }
}