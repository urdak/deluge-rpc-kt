import net.ickis.PublishKotlinJarPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

allprojects {
    group = "net.ickis"
    extra["coroutinesVersion"] = "1.1.1"

    repositories {
        jcenter()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

version = "0.2"

plugins {
    kotlin("jvm") version "1.3.20"
}

plugins.apply(PublishKotlinJarPlugin::class)

val junitVersion = "5.4.0"
val log4jVersion = "2.11.2"

dependencies {
    api(kotlin("stdlib"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${extra["coroutinesVersion"]}")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${extra["coroutinesVersion"]}")
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

tasks.withType<Wrapper> {
    gradleVersion = "5.3.1"
    distributionType = Wrapper.DistributionType.ALL
}
