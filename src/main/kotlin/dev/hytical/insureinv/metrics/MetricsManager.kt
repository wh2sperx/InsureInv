package dev.hytical.insureinv.metrics

import dev.hytical.insureinv.InsureInv
import dev.hytical.insureinv.metrics.detectors.EnvironmentDetector
import org.bstats.bukkit.Metrics
import org.bstats.charts.DrilldownPie
import org.bstats.charts.SimplePie
import org.bukkit.Bukkit
import java.util.concurrent.atomic.AtomicBoolean

class MetricsManager(
    private val plugin: InsureInv,
    private val pluginId: Int
) {
    private val started = AtomicBoolean(false)

    fun start() {
        if (!plugin.configManager.getMetrics()) {
            plugin.logger.info("bStats metrics disabled via config.")
            return
        }

        if (!started.compareAndSet(false, true)) return

        Metrics(plugin, pluginId).also { metrics ->
            registerServerEnvironment(metrics)
            registerPluginVersion(metrics)
            registerOnlineMode(metrics)
            registerJavaInfo(metrics)
            registerPlayerCount(metrics)
            registerOsArchitecture(metrics)
            registerTimezone(metrics)
        }

        plugin.logger.info("bStats metrics enabled.")
    }

    private fun registerServerEnvironment(metrics: Metrics) {
        metrics.addCustomChart(
            DrilldownPie("server_environment") {
                mapOf(
                    EnvironmentDetector.detect().name to mapOf(Bukkit.getVersion() to 1)
                )
            }
        )
    }

    private fun registerPluginVersion(metrics: Metrics) {
        metrics.addCustomChart(
            SimplePie("plugin_version") { plugin.pluginMeta.version }
        )
    }

    private fun registerOnlineMode(metrics: Metrics) {
        metrics.addCustomChart(
            SimplePie("online_mode") { if (Bukkit.getOnlineMode()) "Online" else "Offline" }
        )
    }

    private fun registerJavaInfo(metrics: Metrics) {
        metrics.addCustomChart(
            DrilldownPie("java_information") {
                mapOf(
                    "Java Details" to mapOf(
                        "${
                            System.getProperty(
                                "java.runtime.name",
                                "Unknown"
                            )
                        } (${System.getProperty("sun.arch.data.model", "Unknown")}-bit)" to 1
                    ),
                    "Java Vendor" to mapOf(
                        System.getProperty("java.vendor", "Unknown") to 1
                    ),
                    "Java Version" to mapOf(
                        System.getProperty("java.version", "Unknown") to 1
                    )
                )
            }
        )
    }

    private fun registerPlayerCount(metrics: Metrics) {
        metrics.addCustomChart(
            SimplePie("player_count_range") {
                val count = Bukkit.getOnlinePlayers().size
                when {
                    count == 0 -> "0"
                    count <= 5 -> "1-5"
                    count <= 10 -> "6-10"
                    count <= 20 -> "11-20"
                    count <= 50 -> "21-50"
                    count <= 100 -> "51-100"
                    else -> "100+"
                }
            }
        )
    }

    private fun registerOsArchitecture(metrics: Metrics) {
        metrics.addCustomChart(
            SimplePie("os_architecture") {
                System.getProperty("os.arch")?.let { arch ->
                    if (arch.contains("64")) "64-bit" else "32-bit"
                } ?: "Unknown"
            }
        )
    }

    private fun registerTimezone(metrics: Metrics) {
        metrics.addCustomChart(
            DrilldownPie("server_timezone") {
                val tz = java.util.TimeZone.getDefault()
                val tzId = tz.id
                val continent = tzId.substringBefore('/').takeIf { it.isNotEmpty() } ?: run {
                    when {
                        tzId.startsWith("Atlantic/") -> "Atlantic"
                        else -> "Other"
                    }
                }

                mapOf(continent to mapOf(tzId to 1))
            }
        )
    }
}