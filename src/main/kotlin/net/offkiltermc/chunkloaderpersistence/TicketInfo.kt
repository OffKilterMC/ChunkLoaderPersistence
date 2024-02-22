package net.offkiltermc.chunkloaderpersistence

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ChunkPos

data class TicketInfo(val chunkPos: ChunkPos, val blockPos: BlockPos) {
    fun toJson(): JsonObject {
        val result = JsonObject()
        result.add(chunkKey, chunkPos.toJson())
        result.add(blockKey, blockPos.toJson())
        return result
    }

    private fun ChunkPos.toJson(): JsonObject {
        val root = JsonObject()
        root.add("x", JsonPrimitive(x))
        root.add("z", JsonPrimitive(z))
        return root
    }

    private fun BlockPos.toJson(): JsonObject {
        val root = JsonObject()
        root.add("x", JsonPrimitive(x))
        root.add("y", JsonPrimitive(y))
        root.add("z", JsonPrimitive(z))
        return root
    }

    companion object {
        private const val chunkKey = "chunk"
        private const val blockKey = "key"

        private fun decodeChunkPos(obj: JsonObject): ChunkPos {
            return ChunkPos(
                obj.getAsJsonPrimitive("x").asInt,
                obj.getAsJsonPrimitive("z").asInt
            )
        }

        private fun decodeBlockPos(obj: JsonObject): BlockPos {
            return BlockPos(
                obj.getAsJsonPrimitive("x").asInt,
                obj.getAsJsonPrimitive("y").asInt,
                obj.getAsJsonPrimitive("z").asInt
            )
        }

        fun fromJson(obj: JsonObject): TicketInfo {
            val chunkPos = decodeChunkPos(obj.getAsJsonObject(chunkKey))
            val blockPos = decodeBlockPos(obj.getAsJsonObject(blockKey))
            return TicketInfo(chunkPos, blockPos)
        }
    }
}