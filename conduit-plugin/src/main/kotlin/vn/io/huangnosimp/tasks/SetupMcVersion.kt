package vn.io.huangnosimp.tasks

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import vn.io.huangnosimp.utils.copy
import java.net.URI
import java.nio.file.FileSystems

abstract class SetupMcVersion : BaseTask() {
    @get:Classpath
    abstract val decompiledJar: RegularFileProperty

    @get:Input
    abstract val mcBaseVersion: Property<String>

    @get:OutputDirectory
    abstract val sourcesDir: DirectoryProperty

    @get:OutputDirectory
    abstract val resourcesDir: DirectoryProperty

    @TaskAction
    fun run() {
        val sources = sourcesDir.get().asFile
        val resources = resourcesDir.get().asFile
        val jarUri = URI.create("jar:${decompiledJar.get().asFile.toPath().toUri()}")
        val env = mapOf("create" to "true")
        FileSystems.newFileSystem(jarUri, env).use { fs ->
            copy(fs.getPath("/com"), sources.resolve("com").toPath())
            copy(fs.getPath("/net"), sources.resolve("net").toPath())
            copy(fs.getPath("/assets"), resources.resolve("assets").toPath())
            copy(fs.getPath("/data"), resources.resolve("data").toPath())
            copy(fs.getPath("/META-INF"), resources.resolve("META-INF").toPath())
            copy(fs.getPath("/version.json"), resources.resolve("version.json").toPath())
            copy(fs.getPath("/flightrecorder-config.jfc"), resources.resolve("flightrecorder-config.jfc").toPath())
        }
    }
}
