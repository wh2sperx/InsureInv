package dev.hytical.insureinv.command.subcommands

import dev.hytical.insureinv.command.CommandContext
import dev.hytical.insureinv.command.SubCommand
import dev.hytical.insureinv.utils.PlaceholderUtil
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class ResetCommand : SubCommand {
    override val name = "reset"
    override val permission = "insureinv.admin"
    override val requiresPlayer = false

    override fun execute(context: CommandContext) {
        val sender = context.sender
        val messageManager = context.messageManager
        val storageManager = context.storageManager

        val targetName = context.arg(2)
        if (targetName == null) {
            messageManager.sendMessage(sender, "usage-reset")
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

        val playerData = storageManager.getPlayerData(targetPlayer)
        playerData.updateCharges(0)
        storageManager.savePlayerData(playerData)

        messageManager.sendMessage(
            sender, "admin-reset-success",
            PlaceholderUtil.of("player" to targetPlayer.name)
        )
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            3 -> {
                Bukkit.getOnlinePlayers()
                    .map { it.name }
                    .filter { it.lowercase().startsWith(args[2].lowercase()) }
            }

            else -> emptyList()
        }
    }
}
