package dev.hqng.insureinv.economy

import org.bukkit.OfflinePlayer

interface EconomyProvider {
    fun isAvailable(): Boolean

    fun getBalance(player: OfflinePlayer): Double

    fun hasBalance(player: OfflinePlayer, amount: Double): Boolean

    fun withdraw(player: OfflinePlayer, amount: Double): Boolean

    fun deposit(player: OfflinePlayer, amount: Double): Boolean

    fun formatAmount(amount: Double): String
}