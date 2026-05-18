package dev.hqng.insureinv.command.subcommands

import dev.hqng.insureinv.command.CommandContext
import dev.hqng.insureinv.command.SubCommand
import dev.hqng.insureinv.i18n.PluginLanguage
import dev.hqng.insureinv.utils.PlaceholderUtil
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class InfoCommand : SubCommand {
    override val name = "info"
    override val permission: String? = null
    override val requiresPlayer = false

    override fun execute(context: CommandContext) {
        val sender = context.sender
        val messageManager = context.messageManager
        val storageManager = context.storageManager
        val configManager = context.configManager

        val targetPlayer: Player
        val targetName: String

        val targetArg = context.arg(1)
        if (targetArg != null) {
            if (!sender.hasPermission("insureinv.admin")) {
                messageManager.sendMessage(sender, "no-permission")
                return
            }

            targetPlayer = Bukkit.getPlayerExact(targetArg) ?: run {
                messageManager.sendMessage(
                    sender, "player-not-found",
                    PlaceholderUtil.of("player" to targetArg)
                )
                return
            }
            targetName = targetPlayer.name
        } else {
            val player = context.player
            if (player == null) {
                messageManager.sendMessage(sender, "error-player-only")
                return
            }

            if (!sender.hasPermission("insureinv.use")) {
                messageManager.sendMessage(sender, "no-permission")
                return
            }

            targetPlayer = player
            targetName = player.name
        }

        val playerData = storageManager.getPlayerData(targetPlayer)
        val maxCharges = configManager.getMaxCharges()

        val resolveUuid = (sender as? Player)?.uniqueId ?: targetPlayer.uniqueId
        val statusMessage = PluginLanguage.raw(
            resolveUuid,
            "status.${if (playerData.protectionEnabled) "enabled" else "disabled"}"
        )

        messageManager.sendMessage(sender, "info-header")
        messageManager.sendMessage(
            sender, "info-player",
            PlaceholderUtil.of("player" to targetName)
        )
        messageManager.sendMessage(
            sender, "info-charges",
            PlaceholderUtil.charges(playerData.charges, maxCharges)
        )
        messageManager.sendMessage(
            sender, "info-status",
            PlaceholderUtil.of("status" to statusMessage)
        )
        messageManager.sendMessage(
            sender, "info-total-purchases",
            PlaceholderUtil.stats(playerData.totalChargesPurchased, playerData.protectionActivations)
        )
        messageManager.sendMessage(
            sender, "info-usage-count",
            PlaceholderUtil.stats(playerData.totalChargesPurchased, playerData.protectionActivations)
        )
        messageManager.sendMessage(sender, "info-footer")
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            2 -> {
                if (sender.hasPermission("insureinv.admin")) {
                    Bukkit.getOnlinePlayers()
                        .map { it.name }
                        .filter { it.lowercase().startsWith(args[1].lowercase()) }
                } else {
                    emptyList()
                }
            }

            else -> emptyList()
        }
    }
}
