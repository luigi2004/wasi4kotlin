plugins {
    // Kotlin plugin is applied in subprojects as needed
    kotlin("jvm") version "1.9.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}