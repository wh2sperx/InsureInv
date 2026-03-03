package dev.hytical.insureinv.command

import dev.hytical.insureinv.InsureInvPlugin
import dev.hytical.insureinv.command.subcommands.*
import dev.hytical.insureinv.economy.EconomyManager
import dev.hytical.insureinv.i18n.MessageManager
import dev.hytical.insureinv.managers.ConfigManager
import dev.hytical.insureinv.storages.StorageManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class InsureInvCommand(
    private val plugin: InsureInvPlugin,
    private val configManager: ConfigManager,
    private val storageManager: StorageManager,
    private val economyManager: EconomyManager,
    private val messageManager: MessageManager
) : CommandExecutor, TabCompleter {

    private val subcommands: Map<String, SubCommand>

    init {
        subcommands = buildMap {
            put("buy", BuyCommand())
            put("toggle", ToggleCommand())
            put("info", InfoCommand())
            put("usage", UsageCommand())
            put("config", ConfigCommand())

            put("setlang", SetLangCommand(plugin.i18nManager))
            put("help", HelpCommand())
            put("version", VersionCommand(plugin))
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            executeSubcommand("help", sender, arrayOf("help"))
            return true
        }

        val subcommandName = args[0].lowercase()
        val subcommand = subcommands[subcommandName]

        if (subcommand == null) {
            executeSubcommand("help", sender, arrayOf("help"))
            return true
        }

        executeSubcommand(subcommandName, sender, args.map { it }.toTypedArray())
        return true
    }

    private fun executeSubcommand(name: String, sender: CommandSender, args: Array<String>) {
        val subcommand = subcommands[name] ?: return

        if (subcommand.permission != null && !sender.hasPermission(subcommand.permission!!)) {
            messageManager.sendMessage(sender, "no-permission")
            return
        }

        if (subcommand.requiresPlayer && sender !is Player) {
            messageManager.sendMessage(sender, "error-player-only")
            return
        }

        val context = CommandContext(
            sender = sender,
            args = args,
            plugin = plugin,
            configManager = configManager,
            storageManager = storageManager,
            economyManager = economyManager,
            messageManager = messageManager
        )

        subcommand.execute(context)
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when {
            args.size == 1 -> {
                subcommands.keys
                    .filter { it.startsWith(args[0].lowercase()) }
                    .filter { subcommandName ->
                        val subcommand = subcommands[subcommandName] ?: return@filter false
                        subcommand.permission == null || sender.hasPermission(subcommand.permission!!)
                    }
                    .sorted()
            }

            args.size > 1 -> {
                val subcommandName = args[0].lowercase()
                val subcommand = subcommands[subcommandName] ?: return emptyList()

                if (subcommand.permission != null && !sender.hasPermission(subcommand.permission!!)) {
                    return emptyList()
                }

                subcommand.tabComplete(sender, args.map { it }.toTypedArray())
            }

            else -> emptyList()
        }
    }

    fun getSubcommands(): Map<String, SubCommand> = subcommands
}