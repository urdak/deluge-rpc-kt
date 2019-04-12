rootProject.name = "deluge-rpc-kt"

fun includeProject(name: String) {
    include(name)
    project(":$name").name = "${rootProject.name}-$name"
}

includeProject("samples")
includeProject("rx2")
