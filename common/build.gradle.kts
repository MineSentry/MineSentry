plugins {
    id("buildsrc.convention.common")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.bridger)
    testImplementation(libs.bridger)
    testImplementation(kotlin("test"))
}