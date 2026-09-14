package vn.io.huangnosimp.tasks

import org.eclipse.jgit.api.Git
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import vn.io.huangnosimp.util.copy
import java.net.URI
import java.nio.file.FileSystems

abstract class ExtractToWorkspace : BaseTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val patchedJar: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val remappedJar: RegularFileProperty

    @get:OutputDirectory
    abstract val sourceDir: DirectoryProperty

    @get:OutputDirectory
    abstract val resourceDir: DirectoryProperty

    @TaskAction
    fun run() {
        sourceDir.get().asFile.deleteRecursively()
        resourceDir.get().asFile.deleteRecursively()
        FileSystems.newFileSystem(URI.create("jar:${patchedJar.get().asFile.toPath().toUri()}"), mapOf("create" to "false")).use { fs ->
            copy(
                fs.getPath("/net"),
                sourceDir
                    .get()
                    .asFile
                    .toPath()
                    .resolve("net"),
            )
            copy(
                fs.getPath("/com"),
                sourceDir
                    .get()
                    .asFile
                    .toPath()
                    .resolve("com"),
            )
            copy(
                fs.getPath("/META-INF"),
                resourceDir
                    .get()
                    .asFile
                    .toPath()
                    .resolve("META-INF"),
            )
        }
        FileSystems.newFileSystem(URI.create("jar:${remappedJar.get().asFile.toPath().toUri()}"), mapOf("create" to "false")).use { fs ->
            copy(
                (fs.getPath("/assets")),
                resourceDir
                    .get()
                    .asFile
                    .toPath()
                    .resolve("assets"),
            )
            copy(
                fs.getPath("/data"),
                resourceDir
                    .get()
                    .asFile
                    .toPath()
                    .resolve("data"),
            )
            copy(
                fs.getPath("/flightrecorder-config.jfc"),
                resourceDir
                    .get()
                    .asFile
                    .toPath()
                    .resolve("flightrecorder-config.jfc"),
            )
            copy(
                fs.getPath("/version.json"),
                resourceDir
                    .get()
                    .asFile
                    .toPath()
                    .resolve("version.json"),
            )
        }

        Git
            .init()
            .setDirectory(sourceDir.get().asFile)
            .call()
            .commit()
            .setMessage("Initial commit")
            .call()
        Git
            .init()
            .setDirectory(sourceDir.get().asFile)
            .call()
            .commit()
            .setMessage("Initial commit")
            .call()
    }
}
