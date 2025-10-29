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
            context.source.sendFailure(Component.literal("No such data registry '$registryId'."))
            return -1
        }
        registry.reload()
        context.source.sendSuccess({ Component.literal("Reloaded data registry '$registryId'.") }, true)
        return 0
    }

    private fun registryIdSuggestions(
        context: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val input: String = try {
            val id = ResourceLocationArgument.getId(context, "identifier")
            id.toString().replaceFirst("minecraft:", "")
        } catch (_: Exception) {
            ""
        }

        VesselDataProvider.allRegistries().map { it.id.toString() }.filter { it.contains(input) }.forEach {
            builder.suggest(it)
        }

        return builder.buildFuture()
    }
}