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
    abstract val outDir: DirectoryProperty

    @TaskAction
    fun run() {
        val outDir = outDir.get().asFile
        val jarUri = URI.create("jar:${decompiledJar.get().asFile.toPath().toUri()}")
        val env = mapOf("create" to "true")
        FileSystems.newFileSystem(jarUri, env).use { fs ->
            copy(fs.getPath("/com"), outDir.resolve("java/com").toPath())
            copy(fs.getPath("/net"), outDir.resolve("java/net").toPath())
            copy(fs.getPath("/assets"), outDir.resolve("resources/assets").toPath())
            copy(fs.getPath("/data"), outDir.resolve("resources/data").toPath())
            copy(fs.getPath("/META-INF"), outDir.resolve("resources/META-INF").toPath())
            copy(fs.getPath("/version.json"), outDir.resolve("resources/version.json").toPath())
            copy(fs.getPath("/flightrecorder-config.jfc"), outDir.resolve("resources/flightrecorder-config.jfc").toPath())
        }
    }
}
