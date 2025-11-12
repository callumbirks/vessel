package com.doofcraft.vessel.common.network

import net.minecraft.client.Minecraft

interface ClientNetworkPacketHandler<T: NetworkPacket<T>>: PacketHandler<T> {
    fun handle(packet: T, client: Minecraft)
}