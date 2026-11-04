package buildsrc.convention

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.bundling.Jar

plugins {
    id("com.gradleup.shadow")
}

val shade by configurations.creating

dependencies {
    add("implementation", project(":common"))
    shade(project(":common")) {
        exclude(group = "org.jetbrains.kotlin")
    }
}

tasks.named<Jar>("jar") {
    archiveBaseName.set("${rootProject.name}-${project.name}")
    destinationDirectory.set(layout.buildDirectory.dir("devlibs"))
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("${rootProject.name}-${project.name}")
    archiveClassifier.set("")
    configurations = listOf(shade)
}

// リリース CI 用。配布物や MC バージョン範囲が既定と異なるプロジェクトは各ビルドスクリプトで上書きする
val releaseBaseName = "${rootProject.name}-${project.name}"
val javaToolchain = the<JavaPluginExtension>().toolchain
tasks.register<ReleaseInfoTask>("releaseInfo") {
    group = "release"
    target.convention(project.name)
    platform.convention(project.name.substringBefore('-'))
    gameVersionRange.convention("")
    javaVersion.convention(javaToolchain.languageVersion.map { it.asInt() })
    version.convention(provider { project.version.toString() })
    fileName.convention(version.map { "$releaseBaseName-$it.jar" })
    artifact.convention(tasks.named<ShadowJar>("shadowJar").flatMap { it.archiveFile })
    outputDir.convention(layout.buildDirectory.dir("release"))
}
