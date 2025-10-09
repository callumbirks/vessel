package com.doofcraft.vessel.server.registry

import com.doofcraft.vessel.server.api.data.DataRegistry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/**
 * Provides a general listener for resource and data pack updates notifying the [DataRegistry] listening.
 */
interface DataProvider {
    /**
     * Registers a [DataRegistry] to listen for updates.
     * The updates will automatically happen on the correct sides based on [DataRegistry.type].
     *
     * @param registry The [DataRegistry] being registered.
     */
    fun <T : DataRegistry> register(registry: T, reloadable: Boolean): T

    /**
     * Attempts to find a [DataRegistry] with the given [ResourceLocation].
     * See [DataRegistry.id].
     *
     * @param registryIdentifier The [ResourceLocation]
     * @return The [DataRegistry] if existing.
     */
    fun fromIdentifier(registryIdentifier: ResourceLocation): DataRegistry?

    /**
     * Syncs all of [DataRegistry]s in this provider to a player when requested from the server.
     * This should not be invoked in a single player game instance, the default implementation already makes this check.
     *
     * @param player The [ServerPlayer] being synchronized to the server.
     */
    fun sync(player: ServerPlayer)

    fun doAfterSync(player: ServerPlayer, action: () -> Unit)
}
