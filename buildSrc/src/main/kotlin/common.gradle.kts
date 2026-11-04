package buildsrc.convention

import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

version = "1.3.0"

repositories {
    exclusiveContent {
        forRepository { maven { url = uri("https://jitpack.io") } }
        filter { includeGroup("com.github.MineSentry") }
    }

    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

dependencies {
    implementation("net.kyori:adventure-text-minimessage:4.26.1")
    implementation("net.kyori:adventure-text-serializer-gson:4.26.1")

    implementation("com.github.MineSentry:MineSentryLib:1.1.1")

    implementation("com.akuleshov7:ktoml-core:0.7.1")
    implementation("com.akuleshov7:ktoml-file:0.7.1")
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    testLogging {
        events(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED
        )
    }
}