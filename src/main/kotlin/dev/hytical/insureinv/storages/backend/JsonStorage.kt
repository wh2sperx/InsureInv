package dev.hytical.insureinv.storages.backend

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dev.hytical.insureinv.InsureInv
import dev.hytical.insureinv.managers.ConfigManager
import dev.hytical.insureinv.models.PlayerDataModel
import dev.hytical.insureinv.storages.StorageBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class JsonStorage(
    private val plugin: InsureInv,
    private val configManager: ConfigManager
) : StorageBackend {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val dataMap = ConcurrentHashMap<UUID, PlayerDataModel>()
    private val fileMutex = Mutex()
    private val jsonFile: File by lazy {
        File(configManager.getJsonPath())
    }

    override fun initialize(): Boolean {
        return try {
            jsonFile.parentFile?.mkdirs()

            if (jsonFile.exists()) {
                loadFromFile()
            } else {
                jsonFile.createNewFile()
                saveToFileSync()
            }

            plugin.logger.info("JSON storage initialized successfully at ${jsonFile.absolutePath}")
            true
        } catch (e: Exception) {
            plugin.logger.severe("Failed to initialize JSON storage: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private fun loadFromFile() {
        try {
            if (jsonFile.length() == 0L) {
                return
            }

            FileReader(jsonFile).use { reader ->
                val type = object : TypeToken<Map<String, PlayerDataModel>>() {}.type
                val data: Map<String, PlayerDataModel> = gson.fromJson(reader, type) ?: emptyMap()

                dataMap.clear()
                data.forEach { (uuidStr, playerData) ->
                    dataMap[UUID.fromString(uuidStr)] = playerData
                }
            }
        } catch (e: Exception) {
            plugin.logger.severe("Failed to load JSON data: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun saveToFileSync() {
        try {
            val dataToSave = dataMap.map { (uuid, data) ->
                uuid.toString() to data
            }.toMap()

            FileWriter(jsonFile).use { writer ->
                gson.toJson(dataToSave, writer)
            }
        } catch (e: Exception) {
            plugin.logger.severe("Failed to save JSON data: ${e.message}")
        }
    }

    private suspend fun saveToFile() = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            saveToFileSync()
        }
    }

    override suspend fun loadPlayerData(uuid: UUID): PlayerDataModel? {
        return dataMap[uuid]
    }

    override suspend fun savePlayerData(playerDataModel: PlayerDataModel) {
        dataMap[playerDataModel.uuid] = playerDataModel
        saveToFile()
    }

    override fun close() {
        saveToFileSync()
        plugin.logger.info("JSON storage closed")
    }

    override fun isHealthy(): Boolean {
        return jsonFile.exists() && jsonFile.canRead() && jsonFile.canWrite()
    }

    override fun getName(): String = "JSON"

    fun getAllData(): Map<UUID, PlayerDataModel> = dataMap.toMap()
}