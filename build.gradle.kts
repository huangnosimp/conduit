tasks.register("printVersion") {
    description = "print project version"
    val version = project.version
    doFirst{
        println(version)
    }
}