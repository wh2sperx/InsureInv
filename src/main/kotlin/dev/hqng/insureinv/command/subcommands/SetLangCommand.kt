package dev.hqng.insureinv.command.subcommands

import dev.hqng.insureinv.command.CommandContext
import dev.hqng.insureinv.command.SubCommand
import dev.hqng.insureinv.i18n.I18nManager
import dev.hqng.insureinv.i18n.PluginLanguage
import org.bukkit.command.CommandSender

class SetLangCommand(
    private val i18nManager: I18nManager
) : SubCommand {
    override val name = "setlang"
    override val permission: String? = null
    override val requiresPlayer = true

    override fun execute(context: CommandContext) {
        val player = context.playerOrThrow
        val langCode = context.arg(1)

        if (langCode == null) {
            val currentLang = i18nManager.service.getLanguage(player.uniqueId)
            val available = i18nManager.registry.languages.joinToString(", ")
            PluginLanguage.msg(
                player,
                "system.lang-current",
                mapOf("lang" to currentLang, "available" to available)
            )
            return
        }

        if (!i18nManager.registry.languages.contains(langCode)) {
            val available = i18nManager.registry.languages.joinToString(", ")
            PluginLanguage.msg(
                player,
                "system.lang-invalid",
                mapOf("lang" to langCode, "available" to available)
            )
            return
        }

        i18nManager.storage.write(player.uniqueId, langCode)
        i18nManager.service.invalidate(player.uniqueId)

        PluginLanguage.msg(
            player,
            "system.lang-changed",
            mapOf("lang" to langCode)
        )
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        if (args.size != 2) return emptyList()

        return i18nManager.registry.languages
            .filter { it.lowercase().startsWith(args[1].lowercase()) }
            .sorted()
    }
}

