import net.ickis.PublishKotlinJarPlugin
import java.nio.file.Files

version = "0.3.0"

plugins {
    kotlin("jvm")
}

if (Files.exists(project.rootProject.buildFile.toPath().resolveSibling("local.properties"))) {
    plugins.apply(PublishKotlinJarPlugin::class)
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-rx2:${extra["coroutinesVersion"]}")
    api(rootProject)
}
