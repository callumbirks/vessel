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
        val player = VesselServer.server.playerList.getPlayer(UUID.fromString(ctx.playerUuid))
            ?: return null
        UiManager.open(target, player, args)
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

object UiSetState : UiCommand {
    override val id: String = "ui.set_state"
    override suspend fun run(ctx: UiContext, input: Any?, args: Map<String, Any?>): Any? {
        for ((k, v) in args) if (v != null) ctx.state[k] = v else ctx.state.remove(k)
        return ctx.state
    }
}

object UiRefresh : UiCommand {
    override val id: String = "ui.refresh"
    override suspend fun run(ctx: UiContext, input: Any?, args: Map<String, Any?>): Any? {
        val player = VesselServer.server.playerList.getPlayer(UUID.fromString(ctx.playerUuid))
            ?: return null
        UiManager.service.refreshMenu(player)
        return "ok"
    }
}