package com.doofcraft.vessel.common.api

import com.doofcraft.vessel.common.VesselMod
import com.doofcraft.vessel.common.network.ClientNetworkPacketHandler
import com.doofcraft.vessel.common.network.ReloadComponentRegistryS2CPacket
import com.doofcraft.vessel.common.registry.VesselPackets
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.core.component.DataComponentMap
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.PatchedDataComponentMap

/**
 * A registry which maps Vessel keys to DataComponents, synced from the server to the players.
 * This is intended to be used to sync behaviour of dynamically registered items.
 */
object VesselBehaviourRegistry {
    private val components = hashMapOf<String, DataComponentMap>()

    @JvmStatic
    fun get(key: String): DataComponentMap? = components[key]

    @JvmStatic
    fun <T> get(key: String, component: DataComponentType<T>): T? = components[key]?.get(component)

    @JvmStatic
    fun has(key: String): Boolean = components.containsKey(key)

    @JvmStatic
    fun has(key: String, component: DataComponentType<*>): Boolean = components[key]?.has(component) ?: false

    @JvmStatic
    fun set(key: String, map: DataComponentMap) {
        if (VesselMod.isClient) error { "Cannot call VesselComponentRegistry.set from the client" }
        components[key] = PatchedDataComponentMap(map)
    }

    @JvmStatic
    fun <T> set(key: String, component: DataComponentType<T>, value: T) {
        if (VesselMod.isClient) error { "Cannot call VesselComponentRegistry.set from the client" }
        val map =
            components.computeIfAbsent(key) { PatchedDataComponentMap(DataComponentMap.EMPTY) } as PatchedDataComponentMap
        map.set(component, value)
    }

    @JvmStatic
    fun remove(key: String) {
        if (VesselMod.isClient) error { "Cannot call VesselComponentRegistry.remove from the client" }
        components.remove(key)
    }

    @JvmStatic
    fun remove(key: String, component: DataComponentType<*>) {
        if (VesselMod.isClient) error { "Cannot call VesselComponentRegistry.remove from the client" }
        val map = components[key] as PatchedDataComponentMap? ?: return
        map.remove(component)
    }

    init {
        if (VesselMod.isServer) {
            ServerPlayConnectionEvents.JOIN.register { listener, sender, server ->
                ReloadComponentRegistryS2CPacket(components).sendToPlayer(listener.player)
            }
        }
    }

    fun registerClient() {
        VesselPackets.RELOAD_COMPONENTS_S2C.registerClientHandler(ReloadComponentsPacketHandler)
    }

    @JvmStatic
    fun syncToPlayers() {
        ReloadComponentRegistryS2CPacket(components).sendToAllPlayers()
    }

    private fun reload(components: Map<String, DataComponentMap>) {
        if (VesselMod.isServer) error { "VesselComponentRegistry.reload should only called from the client" }
        this.components.clear()
        for ((k, v) in components) {
            this.components[k] = v
        }
    }

    object ReloadComponentsPacketHandler : ClientNetworkPacketHandler<ReloadComponentRegistryS2CPacket> {
        override fun handle(packet: ReloadComponentRegistryS2CPacket, client: Minecraft) {
            reload(packet.components)
        }
    }
}