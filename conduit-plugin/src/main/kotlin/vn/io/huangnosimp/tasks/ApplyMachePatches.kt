package vn.io.huangnosimp.tasks

import io.codechicken.diffpatch.cli.PatchOperation
import io.codechicken.diffpatch.util.Input
import io.codechicken.diffpatch.util.LogLevel
import io.codechicken.diffpatch.util.Output
import io.codechicken.diffpatch.util.archiver.ArchiveFormat
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class ApplyMachePatches : BaseTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val decompiledServerJar: RegularFileProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val machePatchesDir: DirectoryProperty

    @get:OutputFile
    abstract val patchedServerJar: RegularFileProperty

    @TaskAction
    fun run() {
        val result =
            PatchOperation
                .builder()
                .baseInput(Input.ArchiveMultiInput.archive(ArchiveFormat.ZIP, decompiledServerJar.get().asFile.toPath()))
                .patchesInput(Input.FolderMultiInput(machePatchesDir.get().asFile.toPath()))
                .patchedOutput(Output.ArchiveMultiOutput.archive(ArchiveFormat.ZIP, patchedServerJar.get().asFile.toPath()))
                .logTo(logger::lifecycle)
                .level(LogLevel.INFO)
                .build()
                .operate()
        if (result.exit != 0) {
            throw Exception("Failed to apply ${result.summary?.failedMatches} mache patches")
        }

        logger.lifecycle("Applied ${result.summary?.changedFiles} mache patches")
    }
}
