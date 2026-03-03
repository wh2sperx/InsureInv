package dev.hytical.insureinv.command.subcommands

import dev.hytical.insureinv.command.CommandContext
import dev.hytical.insureinv.command.SubCommand
import org.bukkit.command.CommandSender

class ConfigCommand : SubCommand {
    override val name = "config"
    override val permission = "insureinv.admin"
    override val requiresPlayer = false

    private val children: Map<String, SubCommand> = buildMap {
        put("setprice", SetPriceCommand())
        put("setmax", SetMaxCommand())
        put("reload", ReloadCommand())
    }

    override fun execute(context: CommandContext) {
        val subName = context.arg(1)?.lowercase()

        if (subName == null) {
            context.messageManager.sendMessage(context.sender, "usage-config")
            return
        }

        val child = children[subName]
        if (child == null) {
            context.messageManager.sendMessage(context.sender, "usage-config")
            return
        }

        if (child.permission != null && !context.sender.hasPermission(child.permission!!)) {
            context.messageManager.sendMessage(context.sender, "no-permission")
            return
        }

        if (child.requiresPlayer && context.player == null) {
            context.messageManager.sendMessage(context.sender, "error-player-only")
            return
        }

        child.execute(context)
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when {
            args.size == 2 -> {
                children.keys
                    .filter { it.startsWith(args[1].lowercase()) }
                    .filter { childName ->
                        val child = children[childName] ?: return@filter false
                        child.permission == null || sender.hasPermission(child.permission!!)
                    }
                    .sorted()
            }

            args.size > 2 -> {
                val childName = args[1].lowercase()
                val child = children[childName] ?: return emptyList()

                if (child.permission != null && !sender.hasPermission(child.permission!!)) {
                    return emptyList()
                }

                child.tabComplete(sender, args)
            }

            else -> emptyList()
        }
    }
}
