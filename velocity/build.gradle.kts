plugins {
    id("buildsrc.convention.common")
    id("buildsrc.convention.platform")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.velocity.api)
    compileOnly(libs.bridger)
    // Velocity does not bundle Kotlin; shade stdlib so the plugin can load.
    shade(kotlin("stdlib"))
}

kotlin {
    jvmToolchain(25)
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
    )
    inputs.properties(props)
    filesMatching("velocity-plugin.json") {
        expand(props)
    }
}
