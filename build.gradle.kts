import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "net.ickis"
version = "0.1-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.3.20"
}

allprojects {
    repositories {
        jcenter()
    }
}

extra["coroutinesVersion"] = "1.1.1"
val junitVersion = "5.4.0"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${extra["coroutinesVersion"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${extra["coroutinesVersion"]}")
    implementation("net.ickis:rencode-kt:1.0")
    testImplementation("io.mockk:mockk:1.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.getting(Test::class) {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
