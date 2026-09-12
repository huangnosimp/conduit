package vn.io.huangnosimp.tasks

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import vn.io.huangnosimp.util.copy
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@CacheableTask
abstract class ExtractBundlerJar : BaseTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val bundleJar: RegularFileProperty

    @get:OutputFile
    abstract val serverJar: RegularFileProperty

    @get:OutputDirectory
    abstract val libsDir: DirectoryProperty

    @TaskAction
    fun run() {
        val jarUri = URI.create("jar:${bundleJar.get().asFile.toPath().toUri()}")
        val env = mapOf("create" to "false")

        FileSystems.newFileSystem(jarUri, env).use { fileSystem ->
            val serverJarPath =
                Files
                    .readAllLines(fileSystem.getPath("META-INF/versions.list"))[0]
                    .split('\t')
                    .last()
                    .trim()
            val sourceEntry = fileSystem.getPath("/META-INF/versions/$serverJarPath")
            Files.copy(sourceEntry, serverJar.get().asFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

            val sourceLibs = fileSystem.getPath("META-INF/libraries")
            val targetLibs = libsDir.get().asFile.toPath()
            copy(sourceLibs, targetLibs)
        }
    }
}
