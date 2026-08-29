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
