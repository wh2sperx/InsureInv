package dev.hytical.insureinv.i18n

import dev.hytical.i18n.I18nBootstrap
import dev.hytical.i18n.LangRegistry
import dev.hytical.i18n.LangService
import dev.hytical.i18n.LangStorage
import dev.hytical.i18n.bukkit.PdcLangStorage
import dev.hytical.insureinv.InsureInvPlugin
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Logger

class I18nManager(
    private val plugin: InsureInvPlugin,
    private val defaultLanguage: String = "en_US"
) {
    private val bootstrapRef = AtomicReference<I18nBootstrap>()
    @Volatile
    private var _storage: LangStorage? = null
    private val logger: Logger = plugin.logger

    private val langDir = File(plugin.dataFolder, "lang")
    private val localesFile = File(plugin.dataFolder, "locales.yml")

    val registry: LangRegistry
        get() = (bootstrapRef.get()
            ?: throw IllegalStateException("I18nManager not initialized: call initialize() before accessing registry"))
            .registry()

    val service: LangService
        get() = (bootstrapRef.get()
            ?: throw IllegalStateException("I18nManager not initialized: call initialize() before accessing service"))
            .service()

    val storage: LangStorage
        get() = _storage
            ?: throw IllegalStateException("I18nManager not initialized: call initialize() before accessing storage")

    fun initialize() {
        extractDefaults()
        _storage = PdcLangStorage(plugin)
        val newBootstrap = buildNew(_storage!!)
        bootstrapRef.set(newBootstrap)
        logger.info("i18n engine initialized with ${registry.languages.size} language(s): ${registry.languages}")
    }

    fun rebuild() {
        val currentStorage = _storage
            ?: throw IllegalStateException("Cannot rebuild before initialize()")
        val oldBootstrap = bootstrapRef.get()
        val newBootstrap = buildNew(currentStorage)
        bootstrapRef.set(newBootstrap)
        oldBootstrap?.service()?.invalidateAll()
        logger.info("i18n engine rebuilt with ${registry.languages.size} language(s)")
    }

    fun shutdown() {
        bootstrapRef.get()?.service()?.invalidateAll()
    }

    private fun extractDefaults() {
        if (!langDir.exists()) {
            langDir.mkdirs()
        }

        if (!localesFile.exists()) {
            plugin.saveResource("locales.yml", false)
        }

        val defaultLangFiles = listOf("default.yml", "en_US.yml", "vi_VN.yml", "fr_FR.yml", "ja_JP.yml", "en_GB.yml")
        for (fileName in defaultLangFiles) {
            val target = File(langDir, fileName)
            if (!target.exists()) {
                plugin.saveResource("lang/$fileName", false)
            }
        }
    }

    private fun buildNew(storage: LangStorage): I18nBootstrap {
        val defaultFile = File(langDir, "default.yml")
        if (!defaultFile.exists()) {
            throw IllegalStateException("Missing default language file: ${defaultFile.absolutePath}")
        }

        val builder = I18nBootstrap.builder()
            .defaultLanguage(defaultLanguage)
            .defaultFile { FileInputStream(defaultFile) }
            .storage(storage)

        discoverLocales().forEach { (langKey, langFile) ->
            if (langKey != defaultLanguage) {
                builder.locale(langKey) { FileInputStream(langFile) }
            }
        }

        return builder.build()
    }

    private fun discoverLocales(): List<Pair<String, File>> {
        val locales = mutableListOf<Pair<String, File>>()

        if (!localesFile.exists()) {
            logger.warning("locales.yml not found, no languages will be loaded")
            return locales
        }

        val config = YamlConfiguration.loadConfiguration(localesFile)
        val languagesSection = config.getConfigurationSection("languages")

        if (languagesSection == null) {
            logger.warning("No 'languages' section found in locales.yml")
            return locales
        }

        for (langKey in languagesSection.getKeys(false)) {
            val fileName = languagesSection.getString("$langKey.file")
            if (fileName == null) {
                logger.warning("Language '$langKey' in locales.yml is missing 'file' property, skipping")
                continue
            }

            val langFile = File(langDir, fileName)
            if (!langFile.exists()) {
                logger.warning("Language file '$fileName' for '$langKey' not found in ${langDir.absolutePath}, skipping")
                continue
            }

            locales.add(langKey to langFile)
        }

        return locales
    }
}


