package com.doofcraft.vessel.api

import com.doofcraft.vessel.base.VesselBaseBlockEntity
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents
import net.minecraft.server.level.ServerLevel

object VesselEvents {
    /**
     * Unlike Fabric's `ServerBlockEntityEvents.BLOCK_ENTITY_LOAD`, this will be called AFTER data has been loaded.
     */
    @JvmField
    val BLOCK_ENTITY_LOAD: Event<BlockEntityLoad> =
        EventFactory.createArrayBacked(BlockEntityLoad::class.java) { callbacks: Array<BlockEntityLoad> ->
            BlockEntityLoad { blockEntity: VesselBaseBlockEntity, level: ServerLevel ->
                for (callback in callbacks) {
                    callback.onLoad(blockEntity, level)
                }
            }
        };

    /**
     * Essentially identical to Fabric's `ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD`, but only for Vessel blocks.
     */
    @JvmField
    val BLOCK_ENTITY_UNLOAD: Event<BlockEntityUnload> =
        EventFactory.createArrayBacked(BlockEntityUnload::class.java) { callbacks: Array<BlockEntityUnload> ->
            BlockEntityUnload { blockEntity: VesselBaseBlockEntity, level: ServerLevel ->
                for (callback in callbacks) {
                    callback.onUnload(blockEntity, level)
                }
            }
        }

    init {
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register { blockEntity, level ->
            if (blockEntity is VesselBaseBlockEntity) {
                BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, level)
            }
        }
    }

    fun interface BlockEntityLoad {
        fun onLoad(blockEntity: VesselBaseBlockEntity, level: ServerLevel)
    }

    fun interface BlockEntityUnload {
        fun onUnload(blockEntity: VesselBaseBlockEntity, level: ServerLevel)
    }
}