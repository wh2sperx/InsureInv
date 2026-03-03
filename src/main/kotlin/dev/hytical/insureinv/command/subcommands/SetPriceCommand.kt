package dev.hytical.insureinv.command.subcommands

import dev.hytical.insureinv.command.CommandContext
import dev.hytical.insureinv.command.SubCommand
import dev.hytical.insureinv.utils.PlaceholderUtil
import org.bukkit.command.CommandSender

class SetPriceCommand : SubCommand {
    override val name = "setprice"
    override val permission = "insureinv.admin"
    override val requiresPlayer = false

    override fun execute(context: CommandContext) {
        val sender = context.sender
        val messageManager = context.messageManager
        val configManager = context.configManager

        val price = context.argDouble(2)
        if (price == null || price <= 0) {
            if (context.arg(2) == null) {
                messageManager.sendMessage(sender, "usage-setprice")
            } else {
                messageManager.sendMessage(sender, "invalid-amount")
            }
            return
        }

        configManager.setPricePerCharge(price)
        messageManager.sendMessage(
            sender, "price-updated",
            PlaceholderUtil.of("price" to String.format("%.2f", price))
        )
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            3 -> listOf("100", "200", "500").filter { it.startsWith(args[2]) }
            else -> emptyList()
        }
    }
}
