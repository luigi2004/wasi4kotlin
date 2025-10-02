plugins {
    kotlin("jvm")
}

repositories { mavenCentral() }

dependencies {
    // Keep example lightweight; tests live in the library module
    implementation(project(":wit-wasi-kotlin"))
}