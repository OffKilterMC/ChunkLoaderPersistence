package net.offkiltermc.chunkloaderpersistence

import com.mojang.brigadier.CommandDispatcher
import com.mojang.logging.LogUtils
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopping
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.core.BlockPos
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.TicketType
import net.minecraft.world.level.ChunkPos

class ChunkLoaderPersistence : ModInitializer {
    override fun onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { server: MinecraftServer ->
            serverStarted(
                server
            )
        })
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerStopping { server: MinecraftServer -> serverStopping(server) })

        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<CommandSourceStack>, registryAccess: CommandBuildContext, environment: Commands.CommandSelection ->
            TicketCommands.register(
                dispatcher
            )
        })

    }

    companion object {
        private val LOGGER = LogUtils.getLogger()

        private fun serverStarted(server: MinecraftServer) {
            DataFile.INSTANCE.load(server)?.let { map ->
                server.allLevels.forEach { level ->
                    val dimension = level.dimension().identifier()
                    map[dimension]?.let { tickets ->
                        tickets.forEach {
                            LOGGER.info("Restoring portal ticket for $it in $dimension")
                            level.chunkSource.addTicketWithRadius(TicketType.PORTAL, it.chunkPos, 3)
                        }
                    }
                }
            }
        }

        private fun serverStopping(server: MinecraftServer) {
            val map = mutableMapOf<Identifier, List<TicketInfo>>()

            for (level in server.allLevels) {
                val dm = level.chunkSource.chunkMap.distanceManager
                val tt = dm.ticketStorage
                val tickets = tt.tickets
                val list: MutableList<TicketInfo> = ArrayList()
                for (key in tickets.keys) {
                    val pos = ChunkPos(key)
                    val s = tickets[key]
                    for (t in s) {
                        if (t.type === TicketType.PORTAL) {
                            list.add(TicketInfo(pos, BlockPos.of(key)))
                        }
                    }
                }

                map[level.dimension().identifier()] = list
            }

            LOGGER.info("Saving portal tickets")
            DataFile.INSTANCE.save(map, server)
        }
    }
}
