plugins {
    // Kotlin plugin is applied in subprojects as needed
    kotlin("jvm") version "1.9.22" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}