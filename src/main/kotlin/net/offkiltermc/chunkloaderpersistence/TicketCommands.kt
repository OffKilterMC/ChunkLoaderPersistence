package net.offkiltermc.chunkloaderpersistence

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.world.level.ChunkPos

object TicketCommands {

    fun register(commandDispatcher: CommandDispatcher<CommandSourceStack>) {
        val builder = Commands.literal("tickets")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(
                Commands.literal("loaded")
                    .executes { context: CommandContext<CommandSourceStack> ->
                        reportLoadedChunks(
                            context.source
                        )
                    })
        commandDispatcher.register(builder)
    }

    private fun reportLoadedChunks(sourceStack: CommandSourceStack): Int {
        val serverLevel = sourceStack.level
        val dm = serverLevel.chunkSource.chunkMap.distanceManager
        val tt = dm.ticketStorage
        val tickets = tt.tickets
        for (entry in tickets) {
            val pos = ChunkPos(entry.key)
            val s = tickets[entry.key]
            for (t in s) {
                sourceStack.sendSuccess({
                    Component.literal(
                        String.format("Ticket for %s: %s", pos, t.toString())
                    )
                }, false)
            }
        }
        return 0
    }
}