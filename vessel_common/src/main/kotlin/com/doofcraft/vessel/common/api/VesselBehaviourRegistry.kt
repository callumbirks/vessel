package com.doofcraft.vessel.common.api

import com.doofcraft.vessel.common.VesselMod
import com.doofcraft.vessel.common.network.ClientNetworkPacketHandler
import com.doofcraft.vessel.common.network.ReloadBehavioursS2CPacket
import com.doofcraft.vessel.common.network.SetBehaviourS2CPacket
import com.doofcraft.vessel.common.reactive.SimpleObservable
import com.doofcraft.vessel.common.registry.VesselPackets
import com.doofcraft.vessel.common.serialization.VesselJSON
import com.doofcraft.vessel.common.serialization.adapters.GsonSerializer
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

    val CHANGED = SimpleObservable<Map<String, DataComponentMap>>()

    @JvmStatic
    fun get(key: String): DataComponentMap? = components[key]

    @JvmStatic
    fun <T> get(key: String, component: DataComponentType<T>): T? = components[key]?.get(component)

    @JvmStatic
    fun has(key: String): Boolean = components.containsKey(key)

    @JvmStatic
    fun <T> has(key: String, component: DataComponentType<T>): Boolean = components[key]?.has(component) ?: false

    @JvmStatic
    fun set(key: String, map: DataComponentMap) {
        if (VesselMod.isClient) error { "Cannot call VesselComponentRegistry.set from the client" }
        components[key] = map as? PatchedDataComponentMap ?: PatchedDataComponentMap(map)
        // After each set we need to sync the new behaviour to the players
        SetBehaviourS2CPacket(key, map).sendToAllPlayers()
        CHANGED.emit(mapOf(key to map))
    }

    @JvmStatic
    fun <T> set(key: String, component: DataComponentType<T>, value: T) {
        if (VesselMod.isClient) error { "Cannot call VesselComponentRegistry.set from the client" }
        val map =
            components.computeIfAbsent(key) { PatchedDataComponentMap(DataComponentMap.EMPTY) } as PatchedDataComponentMap
        map.set(component, value)
        // After each set we need to sync the new behaviour to the players
        SetBehaviourS2CPacket(key, map).sendToAllPlayers()
        CHANGED.emit(mapOf(key to map))
    }

    @JvmStatic
    fun remove(key: String) {
        if (VesselMod.isClient) error { "Cannot call VesselComponentRegistry.remove from the client" }
        if (components.remove(key) != null) {
            SetBehaviourS2CPacket(key, DataComponentMap.EMPTY).sendToAllPlayers()
            CHANGED.emit(mapOf(key to DataComponentMap.EMPTY))
        }
    }

    @JvmStatic
    fun <T> remove(key: String, component: DataComponentType<T>) {
        if (VesselMod.isClient) error { "Cannot call VesselComponentRegistry.remove from the client" }
        val map = components[key] as PatchedDataComponentMap? ?: return
        map.remove(component)
        SetBehaviourS2CPacket(key, map).sendToAllPlayers()
        CHANGED.emit(mapOf(key to map))
    }

    init {
        ServerPlayConnectionEvents.JOIN.register { listener, sender, server ->
            ReloadBehavioursS2CPacket(components).sendToPlayer(listener.player)
        }
    }

    fun registerClient() {
        VesselPackets.RELOAD_BEHAVIOURS_S2C.registerClientHandler(ReloadComponentsPacketHandler)
        VesselPackets.SET_BEHAVIOURS_S2C.registerClientHandler(SetComponentsPacketHandler)
    }

    @JvmStatic
    fun syncToPlayers() {
        ReloadBehavioursS2CPacket(components).sendToAllPlayers()
    }

    private fun reload(components: Map<String, DataComponentMap>) {
        if (VesselMod.isServer) error { "VesselComponentRegistry.reload should only called from the client" }
        this.components.clear()
        for ((k, v) in components) {
            this.components[k] = v
        }
        CHANGED.emit(components)
    }

    object ReloadComponentsPacketHandler : ClientNetworkPacketHandler<ReloadBehavioursS2CPacket> {
        override fun handle(packet: ReloadBehavioursS2CPacket, client: Minecraft) {
            VesselMod.LOGGER.info("RELOAD_BEHAVIOURS_S2C {}", packet.components.map { (k, v) ->
                k to VesselJSON.JSON.encodeToString(
                    GsonSerializer, DataComponentMap.CODEC.encodeStart(VesselJSON.MINECRAFT_JSON_OPS, v).getOrThrow()
                )
            })
            reload(packet.components)
        }
    }

    object SetComponentsPacketHandler : ClientNetworkPacketHandler<SetBehaviourS2CPacket> {
        override fun handle(packet: SetBehaviourS2CPacket, client: Minecraft) {
            VesselMod.LOGGER.info(
                "SET_BEHAVIOURS_S2C {} {}", packet.key, VesselJSON.JSON.encodeToString(
                    GsonSerializer,
                    DataComponentMap.CODEC.encodeStart(VesselJSON.MINECRAFT_JSON_OPS, packet.components).getOrThrow()
                )
            )
            if (packet.components.isEmpty) {
                components.remove(packet.key)
            } else {
                components[packet.key] = packet.components
            }
            CHANGED.emit(mapOf(packet.key to packet.components))
        }
    }
}