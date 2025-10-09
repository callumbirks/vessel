package com.doofcraft.vessel.server.ui.render

import com.doofcraft.vessel.common.api.VesselIdentifier
import com.doofcraft.vessel.server.ui.expr.Scope
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ResolvableProfile
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

object PlayerHeadUiComponentMapper : UiComponentMapper {
    override fun shouldApply(itemId: VesselIdentifier, stack: ItemStack, scope: Scope, player: ServerPlayer): Boolean {
        if (itemId.toString() != "minecraft:player_head") return false
        val uuidStr = (scope.value as? Map<*, *>)?.get("uuid")?.toString()
        return !uuidStr.isNullOrEmpty()
    }

    override fun apply(itemId: VesselIdentifier, stack: ItemStack, scope: Scope, player: ServerPlayer) {
        val uuidStr = (scope.value as? Map<*, *>)?.get("uuid")?.toString() ?: return
        val uuid = runCatching { UUID.fromString(uuidStr) }.getOrNull() ?: return

        val profileCache = player.server.profileCache

        val profile = if (profileCache == null) {
            VesselMod.LOGGER.warn("MinecraftServer.profileCache is null!")
            val sp = player.server.playerList.getPlayer(uuid) ?: return
            ResolvableProfile(sp.gameProfile)
        } else {
            val gp = profileCache.get(uuid).getOrNull() ?: return
            ResolvableProfile(gp)
        }

        stack[DataComponents.PROFILE] = profile
    }
}