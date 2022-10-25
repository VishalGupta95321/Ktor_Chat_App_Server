val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.7.10"
}

group = "com.example"
version = "0.0.1"
application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-core-jvm:2.1.2")
    implementation("io.ktor:ktor-server-websockets-jvm:2.1.2")
    implementation("io.ktor:ktor-serialization-gson:2.1.2")
    implementation("io.ktor:ktor-server-sessions-jvm:2.1.2")
    implementation("io.ktor:ktor-server-netty-jvm:2.1.2")
    implementation("io.ktor:ktor-server-call-logging:2.1.2")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.1.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    implementation ("com.google.code.gson:gson:2.9.0")
    testImplementation("io.ktor:ktor-server-tests-jvm:2.1.2")
}