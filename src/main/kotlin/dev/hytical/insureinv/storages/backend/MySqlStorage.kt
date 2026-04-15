package dev.hytical.insureinv.storages.backend

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.hytical.insureinv.InsureInv
import dev.hytical.insureinv.managers.ConfigManager
import dev.hytical.insureinv.models.PlayerDataModel
import dev.hytical.insureinv.storages.StorageBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.ResultSet
import java.util.*

class MySqlStorage(
    private val plugin: InsureInv,
    private val configManager: ConfigManager
) : StorageBackend {

    private var dataSource: HikariDataSource? = null

    override fun initialize(): Boolean {
        return try {
            val config = HikariConfig().apply {
                jdbcUrl =
                    "jdbc:mysql://${configManager.getMySQLHost()}:${configManager.getMySQLPort()}/${configManager.getMySQLDatabase()}"
                username = configManager.getMySQLUsername()
                password = configManager.getMySQLPassword()
                driverClassName = "com.mysql.cj.jdbc.Driver"

                maximumPoolSize = configManager.getMySQLMaxPoolSize()
                minimumIdle = configManager.getMySQLMinIdle()
                connectionTimeout = configManager.getMySQLConnectionTimeout()
                idleTimeout = configManager.getMySQLIdleTimeout()
                maxLifetime = configManager.getMySQLMaxLifetime()

                addDataSourceProperty("cachePrepStmts", "true")
                addDataSourceProperty("prepStmtCacheSize", "250")
                addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
                addDataSourceProperty("useServerPrepStmts", "true")
                addDataSourceProperty("useLocalSessionState", "true")
                addDataSourceProperty("rewriteBatchedStatements", "true")
                addDataSourceProperty("cacheResultSetMetadata", "true")
                addDataSourceProperty("cacheServerConfiguration", "true")
                addDataSourceProperty("elideSetAutoCommits", "true")
                addDataSourceProperty("maintainTimeStats", "false")

                poolName = "InsureInv-MySQL-Pool"
            }

            dataSource = HikariDataSource(config)
            createTables()
            plugin.logger.info("MySQL storage initialized successfully")
            true
        } catch (e: Exception) {
            plugin.logger.severe("Failed to initialize MySQL storage: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private fun createTables() {
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS insureinv_players (
                        uuid VARCHAR(36) PRIMARY KEY,
                        username VARCHAR(16) NOT NULL,
                        charges INT NOT NULL DEFAULT 0,
                        protection_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                        total_charges_purchased INT NOT NULL DEFAULT 0,
                        protection_activations INT NOT NULL DEFAULT 0,
                        last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        INDEX idx_username (username)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """.trimIndent()
                )
            }
        }
    }

    private fun getConnection(): Connection {
        return dataSource?.connection ?: throw IllegalStateException("DataSource is not initialized")
    }

    override suspend fun loadPlayerData(uuid: UUID): PlayerDataModel? = withContext(Dispatchers.IO) {
        try {
            getConnection().use { conn ->
                conn.prepareStatement(
                    """
                    SELECT uuid, username, charges, protection_enabled, 
                           total_charges_purchased, protection_activations
                    FROM insureinv_players WHERE uuid = ?
                """.trimIndent()
                ).use { stmt ->
                    stmt.setString(1, uuid.toString())
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            rs.toPlayerData()
                        } else {
                            null
                        }
                    }
                }
            }
        } catch (e: Exception) {
            plugin.logger.severe("Failed to load player data for $uuid: ${e.message}")
            null
        }
    }

    override suspend fun savePlayerData(playerDataModel: PlayerDataModel): Unit = withContext(Dispatchers.IO) {
        try {
            getConnection().use { conn ->
                conn.prepareStatement(
                    """
                    INSERT INTO insureinv_players (uuid, username, charges, protection_enabled, 
                                                total_charges_purchased, protection_activations)
                    VALUES (?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        username = VALUES(username),
                        charges = VALUES(charges),
                        protection_enabled = VALUES(protection_enabled),
                        total_charges_purchased = VALUES(total_charges_purchased),
                        protection_activations = VALUES(protection_activations)
                """.trimIndent()
                ).use { stmt ->
                    stmt.setString(1, playerDataModel.uuid.toString())
                    stmt.setString(2, playerDataModel.username)
                    stmt.setInt(3, playerDataModel.charges)
                    stmt.setBoolean(4, playerDataModel.protectionEnabled)
                    stmt.setInt(5, playerDataModel.totalChargesPurchased)
                    stmt.setInt(6, playerDataModel.protectionActivations)
                    stmt.executeUpdate()
                }
            }
        } catch (e: Exception) {
            plugin.logger.severe("Failed to save player data for ${playerDataModel.uuid}: ${e.message}")
        }
    }

    override fun close() {
        dataSource?.close()
        plugin.logger.info("MySQL storage closed")
    }

    override fun isHealthy(): Boolean {
        return try {
            dataSource?.connection?.use { conn ->
                conn.isValid(5)
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    override fun getName(): String = "MySQL"

    private fun ResultSet.toPlayerData(): PlayerDataModel {
        return PlayerDataModel(
            uuid = UUID.fromString(getString("uuid")),
            username = getString("username"),
            charges = getInt("charges"),
            protectionEnabled = getBoolean("protection_enabled"),
            totalChargesPurchased = getInt("total_charges_purchased"),
            protectionActivations = getInt("protection_activations")
        )
    }
}