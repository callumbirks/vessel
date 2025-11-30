package com.doofcraft.vessel.common.tooltip

import com.doofcraft.vessel.common.VesselMod
import com.doofcraft.vessel.common.network.ClientNetworkPacketHandler
import com.doofcraft.vessel.common.network.ReloadTooltipsS2CPacket
import com.doofcraft.vessel.common.registry.VesselPackets
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object TooltipRegistry {
    private val tooltips = mutableMapOf<String, List<Component>>()

    fun getTooltip(key: String): List<Component>? = tooltips[key]

    fun registerClient() {
        VesselPackets.RELOAD_TOOLTIPS_S2C.registerClientHandler(ReloadTooltipPacketHandler)
    }

    init {
        if (VesselMod.isServer) {
            // When a player joins, send the tooltips
            ServerPlayConnectionEvents.JOIN.register { listener, sender, server ->
                ReloadTooltipsS2CPacket(tooltips).sendToPlayer(listener.player)
            }
        }
    }

    fun reloadFromServer(tooltips: Map<String, List<Component>>) {
        this.tooltips.clear()
        for ((k, v) in tooltips) {
            this.tooltips[k] = v
        }
        ReloadTooltipsS2CPacket(tooltips).sendToAllPlayers()
    }

    private fun reload(tooltips: Map<String, List<Component>>) {
        this.tooltips.clear()
        for ((k, v) in tooltips) {
            this.tooltips[k] = v
        }
    }

    object ReloadTooltipPacketHandler : ClientNetworkPacketHandler<ReloadTooltipsS2CPacket> {
        override fun handle(
            packet: ReloadTooltipsS2CPacket,
            client: Minecraft
        ) {
            reload(packet.tooltips)
        }
    }
}