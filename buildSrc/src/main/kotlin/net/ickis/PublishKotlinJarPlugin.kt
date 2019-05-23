package net.ickis

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig
import com.jfrog.bintray.gradle.BintrayPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.jetbrains.dokka.gradle.DokkaPlugin
import org.jetbrains.dokka.gradle.DokkaTask
import java.nio.file.Files
import java.util.*

class PublishKotlinJarPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(DokkaPlugin::class)
        project.plugins.apply(MavenPublishPlugin::class)
        project.plugins.apply(BintrayPlugin::class)
        val sourcesJar = project.tasks.create<Jar>("sourcesJar") {
            dependsOn(project.tasks["classes"])
            archiveClassifier.set("sources")
            from(project.extensions.getByName<SourceSetContainer>("sourceSets")["main"].allSource)
        }
        val dokkaTask = project.tasks.withType<DokkaTask> {
            reportUndocumented = false
            outputFormat = "javadoc"
            outputDirectory = "${project.buildDir}/javadoc"
        }
        val javadocJar = project.tasks.create<Jar>("javadocJar") {
            dependsOn(dokkaTask)
            archiveClassifier.set("javadoc")
            from(project.buildDir.resolve("javadoc"))
        }
        configurePublishing(project, sourcesJar, javadocJar)
        configureBintray(project)
    }

    private fun configurePublishing(project: Project, sourcesJar: Jar, javadocJar: Jar) {
        project.extensions.configure<PublishingExtension>("publishing") {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(project.components["java"])
                    artifact(sourcesJar)
                    artifact(javadocJar)
                    groupId = project.group.toString()
                    artifactId = project.name
                    version = project.version.toString()
                }
            }
        }
    }

    private fun configureBintray(project: Project) {
        val credentials = readCredentials(project)
        project.extensions.configure<BintrayExtension>("bintray") {
            user = credentials.first
            key = credentials.second
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

    }

    private fun readCredentials(project: Project): Pair<String, String> {
        val path = project.rootProject.buildFile.toPath().resolveSibling("local.properties")
        return Files.newInputStream(path).use {
            val props = Properties()
            props.load(it)
            val user = props.getProperty("bintray.user")
                    ?: throw IllegalStateException("No bintray.user in local.properties")
            val key = props.getProperty("bintray.apikey")
                    ?: throw IllegalStateException("No bintray.apikey in local.properties")
            user to key
        }
    }
}
