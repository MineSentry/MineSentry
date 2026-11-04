import buildsrc.convention.ReleaseInfoTask

plugins {
    id("buildsrc.convention.common")
    id("buildsrc.convention.platform")
    id("net.neoforged.moddev") version "2.0.144"
}

val neoforgeVersion = sc.properties.get<String>("neoforge")
val neoforgeVersionRange = sc.properties.getOrNull<String>("neoforge_version_range") ?: "[${neoforgeVersion},]"
val kotlinForForgeVersion = sc.properties.get<String>("kotlin_for_forge")
val minecraftVersion = sc.current.version
val minecraftVersionRange = sc.properties.getOrNull<String>("minecraft_version_range") ?: "=${minecraftVersion}"

repositories {
    mavenCentral()
    maven("https://thedarkcolour.github.io/KotlinForForge/")
}

dependencies {
    implementation("thedarkcolour:kotlinforforge-neoforge:${kotlinForForgeVersion}")
}

neoForge {
    version = neoforgeVersion

    runs {
        register("client") {
            client()
        }

        register("server") {
            server()
        }
    }

    mods {
        register("minesentry") {
            sourceSet(sourceSets.main.get())
        }
    }
}

tasks.processResources {
    inputs.properties(
        "mod_version" to project.version,
        "minecraft_version" to minecraftVersion,
        "minecraft_version_range" to minecraftVersionRange,
        "neoforge_version" to neoforgeVersion,
        "neoforge_version_range" to neoforgeVersionRange,
        "kotlin_for_forge_version" to kotlinForForgeVersion
    )
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(inputs.properties)
    }
    exclude("fabric.mod.json")
}

kotlin {
    jvmToolchain(25)
}

tasks.named<ReleaseInfoTask>("releaseInfo") {
    gameVersionRange.set(minecraftVersionRange)
}
