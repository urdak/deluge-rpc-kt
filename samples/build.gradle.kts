plugins {
    kotlin("jvm")
}

dependencies {
    implementation(rootProject)
    implementation(project(":deluge-rpc-kt-rx2"))
}
