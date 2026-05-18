package dev.hqng.insureinv.command

import org.bukkit.command.CommandSender

interface SubCommand {
    val name: String
    val permission: String?
    val requiresPlayer: Boolean

    fun execute(context: CommandContext)
    fun tabComplete(sender: CommandSender, args: Array<String>): List<String>
}
