package com.doofcraft.vessel.server.api.events.ui

import net.minecraft.server.level.ServerPlayer

data class ContainerMenuClosedEvent(
    val player: ServerPlayer,
    val containerId: Int
)
