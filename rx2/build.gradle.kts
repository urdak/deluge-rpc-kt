import net.ickis.PublishKotlinJarPlugin

version = "0.1"

plugins {
    kotlin("jvm")
}

plugins.apply(PublishKotlinJarPlugin::class)

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-rx2:${extra["coroutinesVersion"]}")
    api(rootProject)
}
