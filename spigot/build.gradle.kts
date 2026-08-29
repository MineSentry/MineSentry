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
}

kotlin {
    jvmToolchain(17)
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
    )
    filesMatching("plugin.yml") {
        expand(props)
    }
}
