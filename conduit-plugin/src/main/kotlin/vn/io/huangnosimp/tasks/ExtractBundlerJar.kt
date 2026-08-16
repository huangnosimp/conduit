package vn.io.huangnosimp.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import vn.io.huangnosimp.utils.constant.LIBRARIES_DIR
import vn.io.huangnosimp.utils.constant.VERSIONS_LIST
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@CacheableTask
abstract class ExtractBundlerJar: DefaultTask() {
    @get:Classpath
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
            val serverJarPath = Files.readAllLines(fileSystem.getPath(VERSIONS_LIST))[0].split('\t').last().trim()
            val sourceEntry = fileSystem.getPath("/META-INF/versions/$serverJarPath")
            Files.copy(sourceEntry, serverJar.get().asFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

            val sourceRoot = fileSystem.getPath(LIBRARIES_DIR)
            val targetRoot = libsDir.get().asFile.toPath()
            Files.walk(sourceRoot).use { stream ->
                stream.forEach { source ->
                    val relative = sourceRoot.relativize(source).toString()
                    val target = targetRoot.resolve(relative)

                    if (Files.isDirectory(source)) {
                        Files.createDirectories(target)
                    } else {
                        target.parent?.let { Files.createDirectories(it) }
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
                    }
                }
            }
        }
    }
}