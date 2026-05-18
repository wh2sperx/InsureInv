package dev.hqng.insureinv.storages

import dev.hqng.insureinv.models.PlayerDataModel
import java.util.*

interface StorageBackend {

    fun initialize(): Boolean

    fun close()

    suspend fun loadPlayerData(uuid: UUID): PlayerDataModel?

    suspend fun savePlayerData(playerDataModel: PlayerDataModel)

    fun isHealthy(): Boolean

    fun getName(): String
}