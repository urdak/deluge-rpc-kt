group = "${rootProject.group}"
version = "${rootProject.version}"

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(rootProject)
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${rootProject.extra["coroutinesVersion"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${rootProject.extra["coroutinesVersion"]}")
}
