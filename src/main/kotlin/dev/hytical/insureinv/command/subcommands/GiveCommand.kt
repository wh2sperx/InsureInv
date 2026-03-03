package dev.hytical.insureinv.command.subcommands

import dev.hytical.insureinv.command.CommandContext
import dev.hytical.insureinv.command.SubCommand
import dev.hytical.insureinv.utils.PlaceholderUtil
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class GiveCommand : SubCommand {
    override val name = "give"
    override val permission = "insureinv.admin"
    override val requiresPlayer = false

    override fun execute(context: CommandContext) {
        val sender = context.sender
        val messageManager = context.messageManager
        val storageManager = context.storageManager
        val configManager = context.configManager

        val targetName = context.arg(2)
        if (targetName == null) {
            messageManager.sendMessage(sender, "usage-give")
            return
        }

        val targetPlayer = Bukkit.getPlayerExact(targetName)
        if (targetPlayer == null) {
            messageManager.sendMessage(
                sender, "player-not-found",
                PlaceholderUtil.of("player" to targetName)
            )
            return
        }

        val amount = context.argInt(3)
        if (amount == null || amount <= 0) {
            if (context.arg(3) == null) {
                messageManager.sendMessage(sender, "usage-give")
            } else {
                messageManager.sendMessage(sender, "invalid-amount")
            }
            return
        }

        val playerData = storageManager.getPlayerData(targetPlayer)
        val maxCharges = configManager.getMaxCharges()

        if (playerData.charges + amount > maxCharges) {
            val canGive = maxCharges - playerData.charges
            messageManager.sendMessage(
                sender, "admin-give-max-exceeded",
                PlaceholderUtil.of(
                    "player" to targetPlayer.name,
                    "amount" to canGive.toString()
                )
            )
            return
        }

        playerData.addCharges(amount)
        storageManager.savePlayerData(playerData)

        messageManager.sendMessage(
            sender, "admin-give-success",
            PlaceholderUtil.of(
                "player" to targetPlayer.name,
                "amount" to amount.toString(),
                "charges" to playerData.charges.toString(),
                "max" to maxCharges.toString()
            )
        )
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            3 -> {
                Bukkit.getOnlinePlayers()
                    .map { it.name }
                    .filter { it.lowercase().startsWith(args[2].lowercase()) }
            }

            4 -> listOf("1", "5", "10").filter { it.startsWith(args[3]) }
            else -> emptyList()
        }
    }
}
