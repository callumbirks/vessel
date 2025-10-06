package com.doofcraft.vessel.api.data

import com.doofcraft.vessel.api.reactive.SimpleObservable
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager

/**
 * A registry with data provided by a resource or data pack.
 */
interface DataRegistry {

    /**
     * The unique [ResourceLocation] of this registry.
     */
    val id: ResourceLocation

    /**
     * The expected [PackType].
     */
    val type: PackType

    /**
     * An observable that emits whenever this registry has finished reloading.
     */
    val observable: SimpleObservable<out DataRegistry>

    /**
     * Reloads this registry.
     *
     * @param manager The newly updated [ResourceManager]
     */
    fun reload(manager: ResourceManager)

    /**
     * Syncs this registry to a player when requested by the server.
     *
     * @param player The [ServerPlayer] being synchronized to the server.
     */
    fun sync(player: ServerPlayer)

}
