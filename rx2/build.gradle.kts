version = "0.1-SNAPSHOT"

plugins {
    kotlin("jvm")
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-rx2:${extra["coroutinesVersion"]}")
    api(rootProject)
}
