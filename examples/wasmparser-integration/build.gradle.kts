plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":wasmparser"))
    implementation(kotlin("stdlib"))
}

application {
    mainClass.set("com.example.WasmParserIntegrationKt")
}
