package dev.hqng.insureinv.command.subcommands

import dev.hqng.insureinv.command.CommandContext
import dev.hqng.insureinv.command.SubCommand
import dev.hqng.insureinv.i18n.MessageManager
import dev.hqng.insureinv.managers.ConfigManager
import dev.hqng.insureinv.storages.StorageManager
import dev.hqng.insureinv.utils.PlaceholderUtil
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ToggleCommand : SubCommand {
    override val name = "toggle"
    override val permission: String? = null
    override val requiresPlayer = false

    override fun execute(context: CommandContext) {
        val sender = context.sender
        val messageManager = context.messageManager
        val storageManager = context.storageManager
        val configManager = context.configManager

        val targetName = context.arg(1)

        if (targetName != null) {
            if (!sender.hasPermission("insureinv.admin")) {
                messageManager.sendMessage(sender, "no-permission")
                return
            }

            val targetPlayer = Bukkit.getPlayerExact(targetName) ?: run {
                messageManager.sendMessage(
                    sender, "player-not-found",
                    PlaceholderUtil.of("player" to targetName)
                )
                return
            }

            val playerData = toggleAndNotify(targetPlayer, storageManager, configManager, messageManager)

            if (sender != targetPlayer) {
                messageManager.sendMessage(
                    sender, "admin-toggle-success",
                    PlaceholderUtil.of(
                        "status" to if (playerData.protectionEnabled) "on" else "off",
                        "player" to targetPlayer.name
                    )
                )
            }
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

            toggleAndNotify(player, storageManager, configManager, messageManager)
        }
    }

    private fun toggleAndNotify(
        player: Player,
        storageManager: StorageManager,
        configManager: ConfigManager,
        messageManager: MessageManager
    ): dev.hqng.insureinv.models.PlayerDataModel {
        val playerData = storageManager.getPlayerData(player)
        playerData.protectionEnabled = !playerData.protectionEnabled
        storageManager.savePlayerData(playerData)

        val messageKey = if (playerData.protectionEnabled) "toggle-on" else "toggle-off"
        messageManager.sendMessage(
            player, messageKey,
            PlaceholderUtil.charges(playerData.charges, configManager.getMaxCharges())
        )
        return playerData
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
