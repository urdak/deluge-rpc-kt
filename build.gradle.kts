import java.util.Properties
import java.util.Date
import java.nio.file.Files
import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "net.ickis"
version = "0.2-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.3.20"
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
    id("org.jetbrains.dokka") version "0.9.17"
}

allprojects {
    repositories {
        jcenter()
    }
}

extra["coroutinesVersion"] = "1.1.1"
val junitVersion = "5.4.0"
val log4jVersion = "2.11.2"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${extra["coroutinesVersion"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${extra["coroutinesVersion"]}")
    implementation("net.ickis:rencode-kt:1.0")
    implementation("io.github.microutils:kotlin-logging:1.6.24")
    runtime("org.apache.logging.log4j:log4j-core:$log4jVersion")
    runtime("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    testImplementation("io.mockk:mockk:1.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.hamcrest:hamcrest:2.1")
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

/* ------------------------- PUBLISHING ------------------------- */

val sourcesJar = task<Jar>("sourcesJar") {
    dependsOn(tasks["classes"])
    classifier = "sources"
    from(sourceSets["main"].allSource)
}

tasks.withType<DokkaTask> {
    reportUndocumented = false
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/javadoc"
}

val javadocJar = task<Jar>("javadocJar") {
    dependsOn("dokka")
    classifier = "javadoc"
    from(buildDir.resolve("javadoc"))
}

publishing {
    publications {
        create("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }
    }
}

val credentials = Files.newInputStream(rootProject.buildFile.toPath().resolveSibling("local.properties")).use {
    val props = Properties()
    props.load(it)
    props
}

bintray {
    user = credentials["bintray.user"] as? String
    key = credentials["bintray.apikey"] as? String
    pkg(closureOf<PackageConfig> {
        repo = "maven"
        name = project.name
        setLicenses("MIT")
        version(closureOf<VersionConfig> {
            name = project.version.toString()
            released = Date().toString()
        })
        setPublications("mavenJava")
        vcsUrl = "https://github.com/urdak/deluge-rpc-kt"
        publish = true
    })
}
