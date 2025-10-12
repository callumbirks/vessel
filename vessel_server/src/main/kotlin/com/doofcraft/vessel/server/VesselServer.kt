package com.doofcraft.vessel.server

import com.doofcraft.vessel.common.base.VesselBaseBlockEntity
import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.ModBlockEntities
import com.doofcraft.vessel.common.registry.ModBlocks
import com.doofcraft.vessel.common.registry.ModComponents
import com.doofcraft.vessel.common.registry.ModItems
import com.doofcraft.vessel.server.api.VesselRegistry
import com.doofcraft.vessel.server.ui.UiManager
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult

object VesselServer: DedicatedServerModInitializer {
    lateinit var server: MinecraftServer
        private set

    override fun onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            this.server = server
            lateInitialize()
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            ModCommands.register(dispatcher)
        }

        ModComponents.register()
        ModItems.register()
        ModBlocks.register()
        ModBlockEntities.register()
        UseBlockCallback.EVENT.register(::useBlock)
        UiManager.register()
        VesselDataProvider.registerDefaults()
    }

    // Stuff that needs to run after all mods have been loaded and initialized.
    private fun lateInitialize() {
        VesselDataProvider.reloadAll()
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