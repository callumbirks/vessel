package com.doofcraft.vessel.server.ui.render

import com.doofcraft.vessel.common.api.VesselIdentifier
import com.doofcraft.vessel.server.ui.expr.Scope
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

interface UiComponentMapper {
    fun shouldApply(itemId: VesselIdentifier, stack: ItemStack, scope: Scope, player: ServerPlayer): Boolean
    fun apply(itemId: VesselIdentifier, stack: ItemStack, scope: Scope, player: ServerPlayer)
}

object UiComponentMappers {
    private val mappers = mutableListOf<UiComponentMapper>()

    fun register(mapper: UiComponentMapper) { mappers += mapper }
    fun all(): List<UiComponentMapper> = mappers
}