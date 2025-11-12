package com.doofcraft.vessel.common.network

import com.doofcraft.vessel.common.VesselMod
import com.doofcraft.vessel.common.serialization.Encodable
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level

interface NetworkPacket<T : NetworkPacket<T>> : CustomPacketPayload, Encodable {
    val id: ResourceLocation
    override fun type() = CustomPacketPayload.Type<T>(id)

    fun sendToPlayer(player: ServerPlayer) = ServerPlayNetworking.send(player, this)

    fun sendToPlayers(players: Iterable<ServerPlayer>) {
        players.forEach { sendToPlayer(it) }
    }

    fun sendToAllPlayers() {
        VesselMod.server?.playerList?.players?.let { sendToPlayers(it) }
    }

    fun sendToServer() {
        ClientPlayNetworking.send(this)
    }

    fun sendToPlayersAround(
        x: Double,
        y: Double,
        z: Double,
        distance: Double,
        worldKey: ResourceKey<Level>,
        exclusionCondition: (ServerPlayer) -> Boolean = { false }
    ) {
        val server = VesselMod.server ?: return
        server.playerList.players.filter { player ->
            if (exclusionCondition.invoke(player)) return@filter false
            if (player.level().dimension() != worldKey) return@filter false
            val xDiff = x - player.x
            val yDiff = y - player.y
            val zDiff = z - player.z
            return@filter (xDiff * xDiff + yDiff * yDiff + zDiff) < distance * distance
        }.forEach { player -> sendToPlayer(player) }
    }
}