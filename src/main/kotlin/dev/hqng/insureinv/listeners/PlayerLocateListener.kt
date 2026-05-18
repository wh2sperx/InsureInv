package dev.hqng.insureinv.listeners

import dev.hqng.insureinv.i18n.I18nManager
import dev.hqng.insureinv.managers.ConfigManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerLocateListener(
    private val i18nManager: I18nManager,
    private val configManager: ConfigManager
) : Listener {

    @EventHandler(priority = EventPriority.LOW)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (configManager.isPreloadOnJoin()) {
            i18nManager.service.getLanguage(event.player.uniqueId)
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (configManager.isInvalidateOnQuit()) {
            i18nManager.service.invalidate(event.player.uniqueId)
        }
    }
}
