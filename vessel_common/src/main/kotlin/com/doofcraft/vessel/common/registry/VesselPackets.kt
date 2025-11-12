package com.doofcraft.vessel.common.registry

import com.doofcraft.vessel.common.network.NetworkPacket
import com.doofcraft.vessel.common.network.PacketRegisterInfo
import com.doofcraft.vessel.common.network.ReloadTooltipsS2CPacket

object VesselPackets {
    private val packets = mutableListOf<PacketRegisterInfo<*>>()

    fun register() {
        packets.forEach { it.registerPacket() }
    }

    private fun <T : NetworkPacket<T>> create(packet: PacketRegisterInfo<T>): PacketRegisterInfo<T> {
        packets.add(packet)
        return packet
    }

    @JvmField
    val RELOAD_TOOLTIPS_S2C = create(
        PacketRegisterInfo(
            ReloadTooltipsS2CPacket.Companion.ID,
            ReloadTooltipsS2CPacket.Companion::decode,
            PacketRegisterInfo.Direction.Clientbound
        )
    )
}