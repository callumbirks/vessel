package com.doofcraft.vessel

import com.doofcraft.vessel.api.VesselRegistry
import com.doofcraft.vessel.base.VesselBaseBlockEntity
import com.doofcraft.vessel.component.VesselTag
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.world.World

object VesselServerMod: DedicatedServerModInitializer {
    override fun onInitializeServer() {
        UseBlockCallback.EVENT.register(::useBlock)
    }

    fun useBlock(player: PlayerEntity, world: World, hand: Hand, blockHitResult: BlockHitResult): ActionResult {
        if (player.isSpectator || hand != Hand.MAIN_HAND) return ActionResult.PASS
        val blockEntity = world.getBlockEntity(blockHitResult.blockPos)
        if (blockEntity == null || blockEntity !is VesselBaseBlockEntity) return ActionResult.PASS
        val tag = blockEntity.item.get(VesselTag.COMPONENT)
            ?: return ActionResult.PASS
        val block = VesselRegistry.getBlock(tag.key)
            ?: return ActionResult.PASS
        return block.use(world as ServerWorld, blockEntity, player as ServerPlayerEntity, hand)
    }
}