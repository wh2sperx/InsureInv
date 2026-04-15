package dev.hytical.insureinv.economy.providers

import dev.hytical.insureinv.economy.EconomyProvider
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

class VaultProvider(
    private val economy: Economy
) : EconomyProvider {
    override fun isAvailable(): Boolean = true

    override fun getBalance(player: OfflinePlayer): Double = economy.getBalance(player)

    override fun hasBalance(player: OfflinePlayer, amount: Double): Boolean = economy.has(player, amount)

    override fun withdraw(player: OfflinePlayer, amount: Double): Boolean {
        return economy.withdrawPlayer(player, amount).transactionSuccess()
    }

    override fun deposit(player: OfflinePlayer, amount: Double): Boolean {
        return economy.depositPlayer(player, amount).transactionSuccess()
    }

    override fun formatAmount(amount: Double): String = economy.format(amount)

    companion object {
        fun create(): VaultProvider? {
            val pm = Bukkit.getPluginManager()

            val vault = pm.getPlugin("Vault") ?: return null
            if (!vault.isEnabled) return null

            val rsp = Bukkit.getServicesManager()
                .getRegistration(Economy::class.java)
                ?: return null

            val provider = rsp.provider

            return VaultProvider(provider)
        }
    }
}