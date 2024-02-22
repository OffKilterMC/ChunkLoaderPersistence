package net.offkiltermc.chunkloaderpersistence

import com.mojang.logging.LogUtils
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopping
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
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
    }

    companion object {
        private val LOGGER = LogUtils.getLogger()

        private fun serverStarted(server: MinecraftServer) {
            DataFile.INSTANCE.load(server)?.let { map ->
                server.allLevels.forEach { level ->
                    val dimension = level.dimension().location()
                    map[dimension]?.let { tickets ->
                        tickets.forEach {
                            LOGGER.info("Restoring portal ticket for $it in $dimension")
                            level.chunkSource.addRegionTicket(
                                TicketType.PORTAL,
                                it.chunkPos,
                                3,
                                it.blockPos
                            )
                        }
                    }
                }
            }
        }

        private fun serverStopping(server: MinecraftServer) {
            val map = mutableMapOf<ResourceLocation, List<TicketInfo>>()

            for (level in server.allLevels) {
                level.chunkSource.chunkMap.chunks
                val dm = level.chunkSource.chunkMap.distanceManager
                val tt = dm.tickingTicketsTracker
                val tickets = tt.tickets
                val list: MutableList<TicketInfo> = ArrayList()
                for (key in tickets.keys) {
                    val pos = ChunkPos(key)
                    val s = tickets[key]
                    for (t in s) {
                        if (t.type === TicketType.PORTAL) {
                            list.add(TicketInfo(pos, t.key as BlockPos))
                        }
                    }
                }

                map[level.dimension().location()] = list
            }

            LOGGER.info("Saving portal tickets")
            DataFile.INSTANCE.save(map, server)
        }
    }
}
