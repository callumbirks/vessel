package com.doofcraft.vessel.server.api.events

import com.doofcraft.vessel.common.base.VesselBaseBlockEntity
import com.doofcraft.vessel.server.api.events.config.ConfigsLoadedEvent
import com.doofcraft.vessel.server.api.events.ui.ContainerMenuClosedEvent
import com.doofcraft.vessel.server.api.events.ui.ContainerMenuOpenedEvent
import com.doofcraft.vessel.server.api.events.world.BlockEntityLoadEvent
import com.doofcraft.vessel.server.api.events.world.BlockEntityUnloadEvent
import com.doofcraft.vessel.server.api.reactive.SimpleObservable
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents

object VesselEvents {
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
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register { blockEntity, level ->
            if (blockEntity is VesselBaseBlockEntity) {
                BLOCK_ENTITY_UNLOAD.emit(BlockEntityUnloadEvent(blockEntity, level))
            }
        }
    }
}