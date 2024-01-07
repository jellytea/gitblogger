// Copyright 2023-2024 JetERA Creative
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0
// that can be found in the LICENSE file and https://mozilla.org/MPL/2.0/.

plugins {
    kotlin("jvm") version "1.9.21"
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

group = "com.github.jellytea.gitblogger"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:markdown:0.5.0")
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(Pair("Main-Class", "com.github.jellytea.gitblogger.MainKt"))
        attributes(
            Pair("Class-Path", configurations
                .runtimeClasspath
                .get()
                .joinToString(separator = " ") { file ->
                    "libs/${file.name}"
                })
        )
    }
}

tasks.shadowJar {
    mergeServiceFiles()
}

kotlin {
    jvmToolchain(8)
}