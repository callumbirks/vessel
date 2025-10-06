package com.doofcraft.vessel.ui.cmd

import com.doofcraft.vessel.ui.cmd.commands.MapList
import com.doofcraft.vessel.ui.cmd.commands.Take

fun registerUiCommands() {
    listOf(
        Take,
        MapList
    ).forEach(CommandBus::register)
}