plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "it.fraudata-gen"
version = "1.0-SNAPSHOT"

application {

    mainClass.set("it.fraudata.ServerKt") 
}


repositories {
    mavenCentral()
}
val ktorVersion = "2.3.10"

dependencies {

    implementation("net.datafaker:datafaker:2.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // --- KTOR DEPENDENCIES ---
    implementation("io.ktor:ktor-server-core:${ktorVersion}")
    implementation("io.ktor:ktor-server-netty:${ktorVersion}")
    implementation("io.ktor:ktor-server-content-negotiation:${ktorVersion}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${ktorVersion}")
    implementation("io.ktor:ktor-server-swagger:${ktorVersion}")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("io.mockk:mockk:1.13.10")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}