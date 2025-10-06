package com.doofcraft.vessel.api.events.ui

import net.minecraft.server.level.ServerPlayer

data class ContainerMenuClosedEvent(
    val player: ServerPlayer,
    val containerId: Int
)
