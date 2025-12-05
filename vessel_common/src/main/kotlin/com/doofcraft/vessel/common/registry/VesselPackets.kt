package com.doofcraft.vessel.common.registry

import com.doofcraft.vessel.common.network.NetworkPacket
import com.doofcraft.vessel.common.network.PacketRegisterInfo
import com.doofcraft.vessel.common.network.ReloadComponentRegistryS2CPacket
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
            ReloadTooltipsS2CPacket.ID,
            ReloadTooltipsS2CPacket::decode,
            PacketRegisterInfo.Direction.Clientbound
        )
    )

    @JvmField
    val RELOAD_COMPONENTS_S2C = create(
        PacketRegisterInfo(
            ReloadComponentRegistryS2CPacket.ID,
            ReloadComponentRegistryS2CPacket::decode,
            PacketRegisterInfo.Direction.Clientbound
        )
    )
}