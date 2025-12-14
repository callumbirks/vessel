package com.doofcraft.vessel.common.api

import com.doofcraft.vessel.common.api.event.BlockDestroyedEvent
import com.doofcraft.vessel.common.api.event.BlockInteractEvent
import com.doofcraft.vessel.common.api.event.BlockPlacedEvent
import com.doofcraft.vessel.common.api.event.ItemFinishUsingEvent
import com.doofcraft.vessel.common.api.event.ItemStackCreatedEvent
import com.doofcraft.vessel.common.api.event.ItemUseEvent
import com.doofcraft.vessel.common.api.event.ItemUseOnEntityEvent
import com.doofcraft.vessel.common.api.event.ProjectileHitEntityEvent
import com.doofcraft.vessel.common.base.VesselBaseBlockEntity
import com.doofcraft.vessel.common.reactive.EventObservable
import com.doofcraft.vessel.common.reactive.SimpleObservable
import com.doofcraft.vessel.common.registry.BehaviourComponents
import com.doofcraft.vessel.common.util.vesselTag
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemUtils
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult

object VesselEvents {
    val ITEM_STACK_CREATED = SimpleObservable<ItemStackCreatedEvent>()

    val BLOCK_PLACED = SimpleObservable<BlockPlacedEvent>()

    val BLOCK_INTERACT = EventObservable<BlockInteractEvent>()

    val BLOCK_DESTROYED = SimpleObservable<BlockDestroyedEvent>()

    val ITEM_USE = EventObservable<ItemUseEvent>()

    val ITEM_USE_ON_ENTITY = EventObservable<ItemUseOnEntityEvent>()

    val ITEM_FINISH_USING = EventObservable<ItemFinishUsingEvent>()

    val PROJECTILE_HIT_ENTITY = SimpleObservable<ProjectileHitEntityEvent>()

    fun register() {
        // Override item to skip any custom use code if the item should be used instantly (e.g. food or weapon).
        ITEM_USE.subscribe { event ->
            val tag = event.stack.vesselTag() ?: return@subscribe
            if (VesselBehaviourRegistry.has(tag.key, BehaviourComponents.ANIMATED_USE))
                event.result = ItemUtils.startUsingInstantly(event.level, event.player, event.hand)
        }
    }
}