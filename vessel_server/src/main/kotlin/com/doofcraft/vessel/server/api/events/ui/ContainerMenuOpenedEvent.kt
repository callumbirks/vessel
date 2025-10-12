package com.doofcraft.vessel.server.api.events.ui

import net.minecraft.server.level.ServerPlayer

data class ContainerMenuOpenedEvent(
    val player: ServerPlayer,
    val containerId: Int,
)
