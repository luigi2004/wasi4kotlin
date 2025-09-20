plugins {
    kotlin("jvm") version "1.9.0" apply false
}

repositories { mavenCentral() }

dependencies {
    // Keep example lightweight; tests live in the library module
    implementation(project(":wit-wasi-kotlin"))
}