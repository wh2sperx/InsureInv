package dev.hytical.insureinv.economy

import dev.hytical.insureinv.InsureInvPlugin
import dev.hytical.insureinv.metrics.ServerPlatform
import org.bukkit.OfflinePlayer

class EconomyManager(
    private val plugin: InsureInvPlugin,
    private val platform: ServerPlatform
) {
    private lateinit var provider: EconomyProvider

    fun initialize() {
        provider = EconomyRegistry(plugin, platform).resolve()
    }

    fun isAvailable(): Boolean {
        return provider.isAvailable()
    }

    fun getBalance(player: OfflinePlayer): Double {
        return provider.getBalance(player)
    }

    fun hasBalance(player: OfflinePlayer, amount: Double): Boolean {
        return provider.hasBalance(player, amount)
    }

    fun withdraw(player: OfflinePlayer, amount: Double): Boolean {
        return provider.withdraw(player, amount)
    }

    fun deposit(player: OfflinePlayer, amount: Double): Boolean {
        return provider.deposit(player, amount)
    }

    fun formatAmount(amount: Double): String = provider.formatAmount(amount)
}