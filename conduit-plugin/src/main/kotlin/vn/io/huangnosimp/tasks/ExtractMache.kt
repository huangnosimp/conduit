package vn.io.huangnosimp.tasks

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import vn.io.huangnosimp.utils.copy
import java.net.URI
import java.nio.file.FileSystems

abstract class ExtractMache : BaseTask() {
    @get:Classpath
    abstract val mache: RegularFileProperty

    @get:OutputFile
    abstract val macheJson: RegularFileProperty

    @get:OutputDirectory
    abstract val patchesDir: DirectoryProperty

    @TaskAction
    fun run() {
        val jarUri = URI.create("jar:${mache.get().asFile.toPath().toUri()}")
        val env = mapOf("create" to "false")
        FileSystems.newFileSystem(jarUri, env).use { fs ->
            copy(fs.getPath("/mache.json"), macheJson.get().asFile.toPath())
            copy(fs.getPath("/patches"), patchesDir.get().asFile.toPath())
        }
    }
}
