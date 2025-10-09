package com.doofcraft.vessel.server.ui.cmd.commands

import com.doofcraft.vessel.server.VesselServer
import com.doofcraft.vessel.server.ui.UiManager
import com.doofcraft.vessel.server.ui.cmd.UiCommand
import com.doofcraft.vessel.server.ui.cmd.UiContext
import java.util.UUID

object UiNavigate : UiCommand {
    override val id: String = "ui.navigate"
    override suspend fun run(ctx: UiContext, input: Any?, args: Map<String, Any?>): Any? {
        val target = args["target"].toString()
        val menuArgs = args["args"] as? Map<String, *> ?: emptyMap<String, Any?>()
        val player = VesselServer.server.playerList.getPlayer(UUID.fromString(ctx.playerUuid))
            ?: return null
        UiManager.open(target, player, menuArgs)
        return "ok"
    }
}

object UiClose : UiCommand {
    override val id: String = "ui.close"
    override suspend fun run(ctx: UiContext, input: Any?, args: Map<String, Any?>): Any? {
        val player = VesselServer.server.playerList.getPlayer(UUID.fromString(ctx.playerUuid))
            ?: return null
        player.closeContainer()
        return "ok"
    }
}