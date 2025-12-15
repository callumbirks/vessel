package com.doofcraft.vessel.server.api.events

import com.doofcraft.vessel.common.api.VesselEvents
import com.doofcraft.vessel.common.api.item.VesselProjectile
import com.doofcraft.vessel.common.base.VesselBaseBlockEntity
import com.doofcraft.vessel.common.reactive.SimpleObservable
import com.doofcraft.vessel.common.util.vesselTag
import com.doofcraft.vessel.server.api.VesselRegistry
import com.doofcraft.vessel.server.api.events.config.ConfigsLoadedEvent
import com.doofcraft.vessel.server.api.events.ui.ContainerMenuClosedEvent
import com.doofcraft.vessel.server.api.events.ui.ContainerMenuOpenedEvent
import com.doofcraft.vessel.server.api.events.world.BlockEntityLoadEvent
import com.doofcraft.vessel.server.api.events.world.BlockEntityUnloadEvent
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

object VesselServerEvents {
    /**
     * Unlike Fabric's `ServerBlockEntityEvents.BLOCK_ENTITY_LOAD`, this will be called AFTER data has been loaded.
     */
    @JvmField
    val BLOCK_ENTITY_LOAD = SimpleObservable<BlockEntityLoadEvent>()

    /**
     * Essentially identical to Fabric's `ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD`, but only for Vessel blocks.
     */
    @JvmField
    val BLOCK_ENTITY_UNLOAD = SimpleObservable<BlockEntityUnloadEvent>()

    @JvmField
    val CONTAINER_MENU_OPENED = SimpleObservable<ContainerMenuOpenedEvent>()

    @JvmField
    val CONTAINER_MENU_CLOSED = SimpleObservable<ContainerMenuClosedEvent>()

    @JvmField
    val CONFIGS_LOADED = SimpleObservable<ConfigsLoadedEvent>()

    fun register() {
        VesselEvents.BLOCK_PLACED.subscribe { (level, pos, placer, blockEntity) ->
            val block = VesselRegistry.getBlock(blockEntity.tag.key) ?: return@subscribe
            block.onPlaced(level as ServerLevel, pos, placer, blockEntity)
            BLOCK_ENTITY_LOAD.emit(BlockEntityLoadEvent(blockEntity, level))
        }
        VesselEvents.BLOCK_INTERACT.subscribe { event ->
            val block = VesselRegistry.getBlock(event.blockEntity.tag.key) ?: return@subscribe
            event.result =
                block.use(event.level as ServerLevel, event.blockEntity, event.player as ServerPlayer, event.hand)
        }
        VesselEvents.BLOCK_DESTROYED.subscribe { (level, pos, blockEntity) ->
            val block = VesselRegistry.getBlock(blockEntity.tag.key) ?: return@subscribe
            block.onDestroyed(level, pos, blockEntity)
        }
        VesselEvents.ITEM_USE.subscribe { event ->
            val tag = event.stack.vesselTag() ?: return@subscribe
            val item = VesselRegistry.getItem(tag.key) ?: return@subscribe
            event.result = item.use(event.stack, event.level as ServerLevel, event.player as ServerPlayer, event.hand)
        }
        VesselEvents.ITEM_USE_ON_ENTITY.subscribe { event ->
            val tag = event.stack.vesselTag() ?: return@subscribe
            val item = VesselRegistry.getItem(tag.key) ?: return@subscribe
            event.result = item.useOnEntity(event.stack, event.player as ServerPlayer, event.target, event.hand)
        }
        VesselEvents.ITEM_FINISH_USING.subscribe { event ->
            val tag = event.stack.vesselTag() ?: return@subscribe
            val item = VesselRegistry.getItem(tag.key) ?: return@subscribe
            event.result = item.finishUsing(event.stack, event.level as ServerLevel, event.user)
        }
        VesselEvents.ITEM_SLOT_CLICKED.subscribe { event ->
            val tag = event.stack.vesselTag() ?: return@subscribe
            val item = VesselRegistry.getItem(tag.key) ?: return@subscribe
            event.result =
                item.onClicked(event.player as ServerPlayer, event.slot, event.action, event.stack, event.otherStack)
        }
        VesselEvents.PROJECTILE_HIT_ENTITY.subscribe { event ->
            val tag = event.projectile.item.vesselTag() ?: return@subscribe
            val item = VesselRegistry.getOfType<VesselProjectile>(tag.key) ?: return@subscribe
            item.onHitEntity(event.hitResult, event.projectile)
        }
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register { blockEntity, level ->
            if (blockEntity is VesselBaseBlockEntity) {
                BLOCK_ENTITY_UNLOAD.emit(BlockEntityUnloadEvent(blockEntity, level))
            }
        }
    }
}