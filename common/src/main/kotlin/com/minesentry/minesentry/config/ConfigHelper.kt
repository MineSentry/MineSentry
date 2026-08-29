package com.minesentry.minesentry.config

import com.akuleshov7.ktoml.source.decodeFromStream
import com.minesentry.minesentry.MineSentry.TOML
import com.minesentry.minesentry.config.MsConfig.Companion.NOW_CONFIG_VERSION
import java.io.File
import java.io.IOException
import kotlin.jvm.Throws

class ConfigHelper(
    val configFile: File,
) {

    lateinit var config: MsConfig private set


    init {
        saveDefault()
        load()
    }

    @Throws(IOException::class)
    fun reload() {
        load()
    }


    @Throws(IOException::class, SecurityException::class, IllegalStateException::class)
    private fun saveDefault() {
        if (configFile.exists()) return

        configFile.parentFile?.mkdirs()

        object {}.javaClass.classLoader
            .getResourceAsStream("config.toml")
            ?.use { input ->
                configFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            ?: error("Resource not found: config.toml")
    }

    @Throws(IOException::class)
    private fun load() {
        config = TOML.decodeFromStream(configFile.inputStream())

        if (config.configVersion != NOW_CONFIG_VERSION) {
            config.configVersion = NOW_CONFIG_VERSION
            save()
        }
    }

    private fun save() {
        val encoded = TOML.encodeToString(MsConfig.serializer(), config)
        configFile.writeText(encoded)
    }

}