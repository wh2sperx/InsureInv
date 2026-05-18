package dev.hqng.insureinv.i18n

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import java.util.*

object PluginLanguage {
    private lateinit var manager: I18nManager
    private val miniMessage: MiniMessage = MiniMessage.miniMessage()
    private val placeholderPattern = Regex("\\{([^}]+)}")

    fun bind(manager: I18nManager) {
        this.manager = manager
    }

    fun msg(player: Player, address: String, placeholders: Map<String, String> = emptyMap()) {
        val raw = resolve(player.uniqueId, address) ?: return
        val resolved = replacePlaceholders(raw, placeholders)
        player.sendMessage(miniMessage.deserialize(resolved))
    }

    fun msg(uuid: UUID, address: String): String? {
        return resolve(uuid, address)
    }

    fun msgOrDefault(
        player: Player,
        address: String,
        fallback: String,
        placeholders: Map<String, String> = emptyMap()
    ) {
        val raw = resolve(player.uniqueId, address) ?: fallback
        val resolved = replacePlaceholders(raw, placeholders)
        player.sendMessage(miniMessage.deserialize(resolved))
    }

    fun raw(uuid: UUID, address: String, placeholders: Map<String, String> = emptyMap()): String {
        val raw = resolve(uuid, address) ?: "<red>Missing: $address</red>"
        return replacePlaceholders(raw, placeholders)
    }

    fun component(uuid: UUID, address: String, placeholders: Map<String, String> = emptyMap()): Component {
        return miniMessage.deserialize(raw(uuid, address, placeholders))
    }

    private fun resolve(uuid: UUID, address: String): String? {
        return manager.service.get(uuid, address)
    }

    private fun replacePlaceholders(message: String, placeholders: Map<String, String>): String {
        if (placeholders.isEmpty()) return message
        return placeholderPattern.replace(message) { matchResult ->
            val key = matchResult.groupValues[1]
            placeholders[key] ?: matchResult.value
        }
    }
}
