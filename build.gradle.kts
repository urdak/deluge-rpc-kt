import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "net.ickis"
version = "0.1-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.3.20"
}

repositories {
    jcenter()
}

val junitVersion = "5.4.0"
val coroutinesVersion = "1.1.1"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
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

//jar {
//    manifest {
//        attributes 'Main-Class': 'net.ickis.deluge.DelugeClientKt'
//    }
//
//    // This line of code recursively collects and copies all of a project's files
//    // and adds them to the JAR itself. One can extend this task, to skip certain
//    // files or particular types at will
//    from { configurations.compile.collect { it.isDirectory() ? it : zipTree(it) } }
//}
