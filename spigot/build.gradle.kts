import buildsrc.convention.ReleaseInfoTask

plugins {
    id("buildsrc.convention.common")
    id("buildsrc.convention.platform")
}

repositories {
    mavenCentral()
    maven {
        name = "spigotmc-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    compileOnly(libs.spigot.api)
    compileOnly(libs.bridger)
    // Spigot/Paper does not bundle Kotlin; shade stdlib so the plugin can load.
    shade(kotlin("stdlib"))
}

kotlin {
    jvmToolchain(17)
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
    )
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

// plugin.yml の api-version と揃える（上限なし = 最新まで）
tasks.named<ReleaseInfoTask>("releaseInfo") {
    gameVersionRange.set(">=1.16")
}
