package vn.io.huangnosimp.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class CleanConduitCache
    @Inject
    constructor(
        private val fs: FileSystemOperations,
    ) : DefaultTask() {
        @get:Internal
        abstract val cacheDir: DirectoryProperty

        @TaskAction
        fun clean() {
            fs.delete { spec -> spec.delete(cacheDir) }
        }
    }
