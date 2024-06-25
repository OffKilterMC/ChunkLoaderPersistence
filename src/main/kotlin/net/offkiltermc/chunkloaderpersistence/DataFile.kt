package net.offkiltermc.chunkloaderpersistence

import com.google.gson.*
import com.mojang.logging.LogUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource
import java.nio.file.Path

class DataFile() {
    private fun Any?.toJson(): JsonElement {
        return when (this) {
            is Number -> JsonPrimitive(this)
            is Boolean -> JsonPrimitive(this)
            is String -> JsonPrimitive(this)
            is List<*> -> this.toJson()
            is Map<*, *> -> this.toJson()
            is TicketInfo -> this.toJson()
            is JsonElement -> this
            else -> JsonNull.INSTANCE
        }
    }

    private fun List<*>.toJson(): JsonArray {
        val list = JsonArray()
        this.forEach { list.add(it.toJson()) }
        return list
    }

    private fun Map<*, *>.toJson(): JsonObject {
        val root = JsonObject()
        this.forEach {
            root.add(it.key.toString(), it.value.toJson())
        }
        return root
    }

    private fun decodeTickInfoList(array: JsonArray): List<TicketInfo> {
        val result = mutableListOf<TicketInfo>()
        array.forEach {
            result.add(TicketInfo.fromJson(it.asJsonObject))
        }
        return result
    }

    private fun filePath(server: MinecraftServer): Path {
        return server.getWorldPath(LevelResource.ROOT).resolve("portal_tickets.json")
    }

    fun save(data: Map<ResourceLocation, List<TicketInfo>>, server: MinecraftServer) {
        try {
            val jsonData = data.toJson()
            val writer = filePath(server).toFile().writer()
            writer.use {
                val gson = GsonBuilder().setPrettyPrinting().create()
                gson.toJson(jsonData, it)
            }
        } catch (e: Exception) {
            LOGGER.error("Exception saving ticket data: $e.message")
        }
    }

    fun load(server: MinecraftServer): Map<ResourceLocation, List<TicketInfo>>? {
        val map = mutableMapOf<ResourceLocation, List<TicketInfo>>()
        try {
            val root = JsonParser.parseReader(filePath(server).toFile().reader())
            for (entry in root.asJsonObject.entrySet()) {
                val list = decodeTickInfoList(entry.value.asJsonArray)
                map[ResourceLocation.parse(entry.key)] = list
            }

            return map
        } catch (e: Exception) {
            LOGGER.error("Unable to read ticket file: ${e.message}")
        }
        return null
    }

    companion object {
        private val LOGGER = LogUtils.getLogger()

        val INSTANCE: DataFile by lazy {
            DataFile()
        }
    }

}