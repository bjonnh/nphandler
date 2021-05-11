plugins {
    application
    kotlin("jvm") version "1.5.0"
}

group = "net.nprod"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClassName = "net.nprod.nphandler.MainKt"
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.miachm.sods:SODS:1.2.2")
    implementation("org.jgrapht:jgrapht-core:1.5.0")
    implementation("org.jgrapht:jgrapht-io:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.2")
}
