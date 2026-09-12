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
import java.nio.file.FileSystems

@CacheableTask
abstract class ExtractMache : BaseTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val mache: RegularFileProperty

    @get:OutputFile
    abstract val macheJson: RegularFileProperty

    @get:OutputDirectory
    abstract val patchesDir: DirectoryProperty

    @TaskAction
    fun run() {
        val zipPath = mache.get().asFile.toPath()
        val env = mapOf("create" to "false")
        FileSystems.newFileSystem(zipPath, env).use { fs ->
            copy(fs.getPath("/mache.json"), macheJson.get().asFile.toPath())
            copy(fs.getPath("/patches"), patchesDir.get().asFile.toPath())
        }
    }
}
