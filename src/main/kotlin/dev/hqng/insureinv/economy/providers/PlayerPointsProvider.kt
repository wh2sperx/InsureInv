package dev.hqng.insureinv.economy.providers

import dev.hqng.insureinv.economy.EconomyProvider
import org.black_ixx.playerpoints.PlayerPoints
import org.black_ixx.playerpoints.PlayerPointsAPI
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

class PlayerPointsProvider(private val api: PlayerPointsAPI) : EconomyProvider {

    override fun isAvailable(): Boolean = true

    override fun getBalance(player: OfflinePlayer): Double {
        val points = api.look(player.uniqueId)
        return if (points >= 0) points.toDouble() else 0.0
    }

    override fun hasBalance(player: OfflinePlayer, amount: Double): Boolean {
        val points = api.look(player.uniqueId)
        return points >= 0 && points >= amount.toInt()
    }

    override fun withdraw(player: OfflinePlayer, amount: Double): Boolean {
        if (amount <= 0) return false
        val value = amount.toInt()
        if (value <= 0) return false

        val current = api.look(player.uniqueId)
        if (current < 0 || current < value) return false

        return api.take(player.uniqueId, value)
    }

    override fun deposit(player: OfflinePlayer, amount: Double): Boolean {
        if (amount <= 0) return false
        val value = amount.toInt()
        if (value <= 0) return false

        return api.give(player.uniqueId, value)
    }

    override fun formatAmount(amount: Double): String = "%,d".format(amount.toInt())

    companion object {
        fun create(): PlayerPointsProvider? {
            val plugin = Bukkit.getPluginManager().getPlugin("PlayerPoints")
                ?: return null

            if (!plugin.isEnabled) return null

            val api = (plugin as PlayerPoints).api
            return PlayerPointsProvider(api)
        }
    }
}