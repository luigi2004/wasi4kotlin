plugins {
    kotlin("jvm")
}

repositories { mavenCentral() }

dependencies {
    // wasmtime-java coordinate (verify latest version before releasing)
    implementation("io.github.kawamuray:wasmtime:0.40.0")
    testImplementation(kotlin("test"))
}