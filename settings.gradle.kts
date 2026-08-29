pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()

        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }

        maven("https://maven.neoforged.net/releases/") {
            name = "NeoForged"
        }

        maven("https://maven.kikugie.dev/releases") {
            name = "KikuGie"
        }
    }
}

plugins {
    // Use the Foojay Toolchains plugin to automatically download JDKs required by subprojects.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version ("0.9.7")
}

rootProject.name = "MineSentry"
include(
    "common",
    "spigot"
)

stonecutter {
    create("mod") {
        vcsVersion = "fabric-26.2"

        fun mc(version: String, needMappings: Boolean = true, vararg loaders: String = arrayOf("fabric", "neoforge")) {
            loaders.forEach { loader ->
                val bs = if (!needMappings) "build.$loader.older26.gradle.kts" else when(loader) {
                    "fabric" -> "build.fabric-remap.gradle.kts"
                    else -> "build.$loader.gradle.kts"
                }
                version("$loader-$version", version).buildscript(bs)
            }
        }

        properties.load(rootProject.projectDir.resolve("mod/properties.toml"))


        // 26.1以降
        listOf(
            "26.2",
            "26.1", // 26.1.x 対応
        ).forEach { mc(it, false) }

        // 難読化のマッピングが必要なバージョン
        listOf(
            // 1.21.x
            "1.21.11",
            "1.21.5",
            "1.21.4",
            "1.21",

            // 1.20.x
            "1.20.4"
        ).forEach(::mc)

        // NeoForgeが対応していないバージョン (1.20以前)
        listOf(
            // 1.16.x
            "1.16.5",
        ).forEach { mc(it, true, "fabric") }
    }
}
