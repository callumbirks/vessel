package com.doofcraft.vessel.server.commands

import com.doofcraft.vessel.server.VesselDataProvider
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.network.chat.Component
import java.util.concurrent.CompletableFuture

object VesselDataCommand {
    val COMMAND: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("vessel").then(
        Commands.literal("data").then(
            Commands.literal("reload").then(
                Commands.argument("identifier", ResourceLocationArgument.id())
                    .suggests(::registryIdSuggestions)
                    .executes(::runReload)
            )
        )
    )

    private fun runReload(context: CommandContext<CommandSourceStack>): Int {
        val registryId = ResourceLocationArgument.getId(context, "identifier")
        val registry = VesselDataProvider.getRegistry(registryId) ?: run {
            context.source.sendFailure(Component.literal("No such data registry '$registryId'"))
            return -1
        }
        registry.reload()
        return 0
    }

    private fun registryIdSuggestions(
        context: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        VesselDataProvider.allRegistries().forEach {
            builder.suggest(it.id.toString())
        }

        return builder.buildFuture()
    }
}