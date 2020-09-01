plugins {
    kotlin("jvm") version "1.4.0"
}

group = "net.nprod"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.miachm.sods:SODS:1.2.2")
    implementation("org.jgrapht:jgrapht-core:1.5.0")
    implementation("org.jgrapht:jgrapht-io:1.5.0")
}
