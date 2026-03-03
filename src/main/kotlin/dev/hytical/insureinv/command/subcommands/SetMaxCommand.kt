package dev.hytical.insureinv.command.subcommands

import dev.hytical.insureinv.command.CommandContext
import dev.hytical.insureinv.command.SubCommand
import dev.hytical.insureinv.utils.PlaceholderUtil
import org.bukkit.command.CommandSender

class SetMaxCommand : SubCommand {
    override val name = "setmax"
    override val permission = "insureinv.admin"
    override val requiresPlayer = false

    override fun execute(context: CommandContext) {
        val sender = context.sender
        val messageManager = context.messageManager
        val configManager = context.configManager

        val max = context.argInt(2)
        if (max == null || max <= 0) {
            if (context.arg(2) == null) {
                messageManager.sendMessage(sender, "usage-setmax")
            } else {
                messageManager.sendMessage(sender, "invalid-amount")
            }
            return
        }

        configManager.setMaxCharges(max)
        messageManager.sendMessage(
            sender, "max-updated",
            PlaceholderUtil.of("max" to max.toString())
        )
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            3 -> listOf("10", "20", "50").filter { it.startsWith(args[2]) }
            else -> emptyList()
        }
    }
}
