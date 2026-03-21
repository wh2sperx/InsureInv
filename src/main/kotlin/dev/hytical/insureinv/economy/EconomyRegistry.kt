package dev.hytical.insureinv.economy

import dev.hytical.insureinv.InsureInvPlugin
import dev.hytical.insureinv.economy.providers.NoneProvider
import dev.hytical.insureinv.economy.providers.PlayerPointsProvider
import dev.hytical.insureinv.economy.providers.VaultProvider
import dev.hytical.insureinv.economy.providers.VaultUnlockedProvider
import dev.hytical.insureinv.metrics.ServerPlatform
import java.util.logging.Logger

class EconomyRegistry(
    plugin: InsureInvPlugin,
    private val platform: ServerPlatform
) {
    private val logger: Logger = plugin.logger
    private val config = plugin.configManager

    private val priorityList: List<EconomyType>
        get() = when (platform) {
            ServerPlatform.FOLIA -> listOf(
                EconomyType.VAULT_UNLOCKED,
                EconomyType.PLAYER_POINTS,
                EconomyType.NONE
            )
            else -> listOf(
                EconomyType.VAULT,
                EconomyType.VAULT_UNLOCKED,
                EconomyType.PLAYER_POINTS,
                EconomyType.NONE
            )
        }

    fun resolve(): EconomyProvider {
        val preferred = config.getEconomyProviderType()

        if (preferred == EconomyType.NONE) {
            logger.info("Economy provider set to NONE - economy features disabled")
            return NoneProvider
        }

        if (isCompatible(preferred)) {
            createProvider(preferred)?.let {
                logger.info("Using economy provider: $preferred")
                return it
            }
            logger.warning("Preferred economy $preferred not available, trying fallback...")
        } else {
            logger.warning("Preferred economy $preferred is not compatible with $platform, trying fallback...")
        }

        for (type in priorityList) {
            if (type == preferred || type == EconomyType.NONE) continue
            createProvider(type)?.let {
                logger.warning("Using economy provider: $type")
                return it
            }
        }

        logger.severe("No economy provider available. Using NONE provider.")
        return NoneProvider
    }

    private fun isCompatible(type: EconomyType): Boolean {
        if (platform == ServerPlatform.FOLIA && type == EconomyType.VAULT) return false
        return true
    }

    private fun createProvider(type: EconomyType): EconomyProvider? {
        return when (type) {
            EconomyType.VAULT -> VaultProvider.create()
            EconomyType.VAULT_UNLOCKED -> VaultUnlockedProvider.create()
            EconomyType.PLAYER_POINTS -> PlayerPointsProvider.create()
            EconomyType.NONE -> NoneProvider
        }
    }
}