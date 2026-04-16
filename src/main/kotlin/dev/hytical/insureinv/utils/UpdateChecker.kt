package dev.hytical.insureinv.utils

import dev.hytical.insureinv.utils.model.ModrinthVersion
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.logging.Logger

class UpdateChecker(private val logger: Logger) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
    }

    private val projectSlug = "insureinv"
    private val apiUrl = "https://api.modrinth.com/v2/project/$projectSlug/version"

    suspend fun checkForUpdates(currentVersion: String, includeBeta: Boolean = false) {
        try {
            val versions = client.get(apiUrl).body<List<ModrinthVersion>>()

            val targetVersion = if (includeBeta) {
                versions.firstOrNull()
            } else {
                versions.find { it.versionType == "release" }
            }

            if (targetVersion == null) return

            val latestVersion = targetVersion.versionNumber
            val downloadUrl = targetVersion.files.firstOrNull()?.url ?: "Không có URL"

            if (currentVersion != latestVersion) {
                logger.info("""
                    There is a new update for InsureInv!
                    Latest Version: $latestVersion (${targetVersion.versionType}) | Download At: $downloadUrl
                """.trimIndent())
            } else {
                logger.info("You are using the latest version of InsureInv")
            }

        } catch (_: Exception) {
            logger.severe("An error occurred while checking for updates")
        }
    }

    suspend fun close() {
        client.close()
    }
}