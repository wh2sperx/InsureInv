package dev.hytical.insureinv.metrics.detectors

import dev.hytical.insureinv.metrics.ServerPlatform

object EnvironmentDetector {
    val type: ServerPlatform by lazy { detectInternal() }

    fun detect(): ServerPlatform = type

    private fun detectInternal(): ServerPlatform = when {
        classExists("io.papermc.paper.threadedregions.RegionizedServer") -> ServerPlatform.FOLIA
        classExists("io.papermc.paper.configuration.Configuration") ||
                classExists("com.destroystokyo.paper.PaperConfig") -> ServerPlatform.PAPER

        classExists("org.spigotmc.SpigotConfig") -> ServerPlatform.SPIGOT
        else -> ServerPlatform.UNKNOWN
    }

    private fun classExists(name: String): Boolean =
        try {
            Class.forName(name)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
}