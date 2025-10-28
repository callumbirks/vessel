package com.doofcraft.vessel.server

import com.doofcraft.vessel.server.commands.VesselConfigCommand
import com.doofcraft.vessel.server.commands.VesselDataCommand
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack

object ModCommands {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(VesselDataCommand.COMMAND)
        dispatcher.register(VesselConfigCommand.COMMAND)
    }
}