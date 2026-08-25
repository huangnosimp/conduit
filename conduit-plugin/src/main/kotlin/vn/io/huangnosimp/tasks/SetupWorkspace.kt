package vn.io.huangnosimp.tasks

import org.eclipse.jgit.api.Git
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import vn.io.huangnosimp.utils.copy

abstract class SetupWorkspace : BaseTask() {
    @get:InputDirectory
    abstract val baseDir: DirectoryProperty

    @get:OutputDirectory
    abstract val workspaceDir: DirectoryProperty

    @TaskAction
    fun run() {
        val workspace = workspaceDir.get().asFile
        val base = baseDir.get().asFile
        workspace.listFiles()?.forEach { it.deleteRecursively() }
        copy(base.toPath(), workspace.toPath())
        Git.init().setDirectory(workspace).call().use { git ->
            git.add().addFilepattern(".").call()
            git.commit().setMessage("Initial commit").call()
        }
    }
}
