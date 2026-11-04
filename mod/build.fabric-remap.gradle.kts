import buildsrc.convention.ReleaseInfoTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("buildsrc.convention.common")
    id("buildsrc.convention.platform")
    id("net.fabricmc.fabric-loom-remap") version ("1.17.20")
}

val minecraftVersion = sc.current.version
val fabricLoaderVersion = sc.properties.get<String>("fabric_loader")
val fabricLoaderVersionRange = sc.properties.getOrNull<String>("fabric_loader_version_range") ?: ">=${fabricLoaderVersion}"
val fabricApiVersion = sc.properties.get<String>("fabric_api")
val fabricLanguageKotlinVersion = sc.properties.get<String>("fabric_language_kotlin")
val minecraftVersionRange = sc.properties.getOrNull<String>("minecraft_version_range") ?: "=${minecraftVersion}"

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")

    mappings(
        loom.officialMojangMappings()
    )

    modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")

    modImplementation("net.fabricmc:fabric-language-kotlin:${fabricLanguageKotlinVersion}")
}

tasks.processResources {
    inputs.properties(
        "mod_version" to project.version,
        "minecraft_version" to minecraftVersion,
        "minecraft_version_range" to minecraftVersionRange,
        "fabric_loader_version" to fabricLoaderVersion,
        "fabric_loader_version_range" to fabricLoaderVersionRange,
        "fabric_language_kotlin_version" to fabricLanguageKotlinVersion
    )
    filesMatching("fabric.mod.json") {
        expand(inputs.properties)
    }
    exclude("META-INF/neoforge.mods.toml")
}

val shadowJar = tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("dev-shadow")
    destinationDirectory.set(layout.buildDirectory.dir("devlibs"))
}

tasks.named<RemapJarTask>("remapJar") {
    dependsOn(shadowJar)
    inputFile.set(shadowJar.flatMap { it.archiveFile })
    archiveBaseName.set("${rootProject.name}-${project.name}")
}

kotlin {
    jvmToolchain(21)
}

tasks.named<ReleaseInfoTask>("releaseInfo") {
    gameVersionRange.set(minecraftVersionRange)
    artifact.set(tasks.named<RemapJarTask>("remapJar").flatMap { it.archiveFile })
}
