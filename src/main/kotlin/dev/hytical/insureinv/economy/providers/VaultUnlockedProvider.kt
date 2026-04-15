package dev.hytical.insureinv.economy.providers

import dev.hytical.insureinv.economy.EconomyProvider
import net.milkbowl.vault2.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

class VaultUnlockedProvider(
    private val economy: Economy
) : EconomyProvider {
    private val pluginName = "InsureInv"

    override fun isAvailable(): Boolean = true

    @Suppress("DEPRECATION")
    override fun getBalance(player: OfflinePlayer): Double =
        economy.getBalance(pluginName, player.uniqueId).toDouble()

    override fun hasBalance(player: OfflinePlayer, amount: Double): Boolean =
        economy.has(pluginName, player.uniqueId, amount.toBigDecimal())

    override fun withdraw(player: OfflinePlayer, amount: Double): Boolean {
        return economy.withdraw(pluginName, player.uniqueId, amount.toBigDecimal()).transactionSuccess()
    }

    override fun deposit(player: OfflinePlayer, amount: Double): Boolean {
        return economy.deposit(pluginName, player.uniqueId, amount.toBigDecimal()).transactionSuccess()
    }

    @Suppress("DEPRECATION")
    override fun formatAmount(amount: Double): String = economy.format(amount.toBigDecimal())

    companion object {
        fun create(): VaultUnlockedProvider? {
            val pm = Bukkit.getPluginManager()
            /*
             Rename from "VaultUnlocked" to "Vault", because VaultUnlocked still uses the name Vault, so in this
             case it will always return null, causing it to be unable to hook
             into the provider on Folia. Sorry my bad
             */
            val vaultUnlocked = pm.getPlugin("Vault") ?: return null
            if (!vaultUnlocked.isEnabled) return null

            val rsp = Bukkit.getServicesManager()
                .getRegistration(Economy::class.java)
                ?: return null

            val provider = rsp.provider

            return VaultUnlockedProvider(provider)
        }
    }
}
