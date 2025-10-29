package com.doofcraft.vessel.server.commands

import com.doofcraft.vessel.server.api.config.VesselConfigRegistry
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.network.chat.Component
import java.util.concurrent.CompletableFuture

object VesselConfigCommand {
    val COMMAND: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("vessel").then(
        Commands.literal("config").then(
            Commands.literal("reload").then(
                Commands.argument("identifier", ResourceLocationArgument.id())
                    .suggests(::configIdSuggestions)
                    .executes(::runReload)
            )
        )
    )

    private fun runReload(context: CommandContext<CommandSourceStack>): Int {
        val configId = ResourceLocationArgument.getId(context, "identifier")
        val result = VesselConfigRegistry.reload(configId)
        if (result) {
            context.source.sendSuccess({ Component.literal("Config '$configId' reloaded.") }, true)
            return 0
        } else {
            context.source.sendFailure(Component.literal("No such config '$configId'."))
            return -1
        }
    }

    private fun configIdSuggestions(
        context: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val input: String = try {
            val id = ResourceLocationArgument.getId(context, "identifier")
            id.toString().replaceFirst("minecraft:", "")
        } catch (_: Exception) {
            ""
        }

        VesselConfigRegistry.configIds()
            .map { it.toString() }
            .filter { it.contains(input) }
            .forEach { builder.suggest(it) }

        return builder.buildFuture()
    }
}