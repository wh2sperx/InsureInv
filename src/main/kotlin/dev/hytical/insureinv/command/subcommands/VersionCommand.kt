package dev.hytical.insureinv.command.subcommands

import dev.hytical.insureinv.InsureInv
import dev.hytical.insureinv.command.CommandContext
import dev.hytical.insureinv.command.SubCommand
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender

class VersionCommand(
    private val plugin: InsureInv,
) : SubCommand {
    private val mm = MiniMessage.miniMessage()
    private val i = plugin.pluginBuildInfo
    override val name: String = "version"
    override val permission: String = "insureinv.admin"
    override val requiresPlayer: Boolean = false

    override fun execute(context: CommandContext) {
        listOf(
            "<white><color:#FFC0CB>${plugin.pluginBuildInfo.getPluginName(true)} ${i.buildVersion}</color> <white>ʙʏ</white> <gray>${
                plugin.pluginMeta.authors.joinToString(
                    " and "
                )
            }</gray>",
            "<gray>┌─</gray><white>ʙʀᴀɴᴄʜ:</white> <#76D7C4>${i.branch}</#76D7C4>",
            "<gray>├─</gray><white>ᴄᴏᴍᴍɪᴛ:</white> <#48C9B0>${i.commitIdAbbrev}</#48C9B0>",
            "<gray>├─</gray><white>ᴍᴇssᴀɢᴇ:</white> <#A3E4D7>${i.commitMessage}</#A3E4D7>",
            "<gray>├─</gray><white>ʙᴜɪʟᴛ:</white> <#85C1E9>${i.buildTime}</#85C1E9>",
            "<gray>└─</gray><white>ᴅɪʀᴛʏ:</white> <${if (i.isDirty) "red" else "green"}>${if (i.isDirty) "ᴛʀᴜᴇ" else "ғᴀʟsᴇ"}</${if (i.isDirty) "red" else "green"}>"
        ).forEach {
            context.sender.sendMessage(
                mm.deserialize(it)
            )
        }
    }

    override fun tabComplete(
        sender: CommandSender,
        args: Array<String>
    ): List<String> = mutableListOf()
}