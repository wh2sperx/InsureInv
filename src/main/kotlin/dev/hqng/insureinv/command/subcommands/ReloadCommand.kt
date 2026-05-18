package dev.hqng.insureinv.command.subcommands

import dev.hqng.insureinv.command.CommandContext
import dev.hqng.insureinv.command.SubCommand
import dev.hqng.insureinv.utils.PlaceholderUtil
import org.bukkit.command.CommandSender

class ReloadCommand : SubCommand {
    override val name = "reload"
    override val permission = "insureinv.admin"
    override val requiresPlayer = false

    override fun execute(context: CommandContext) {
        val sender = context.sender
        val messageManager = context.messageManager
        val configManager = context.configManager
        val storageManager = context.storageManager

        val oldBackend = storageManager.getCurrentBackendName()
        configManager.reload()
        storageManager.reload()
        context.economyManager.initialize()
        context.plugin.reloadI18n()

        val newBackend = storageManager.getCurrentBackendName()

        messageManager.sendMessage(sender, "reload-complete")

        if (oldBackend != newBackend) {
            messageManager.sendMessage(
                sender, "reload-storage-changed",
                PlaceholderUtil.method(newBackend)
            )
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return emptyList()
    }
}
