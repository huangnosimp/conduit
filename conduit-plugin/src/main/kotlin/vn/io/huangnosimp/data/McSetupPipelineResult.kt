package vn.io.huangnosimp.data

import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import vn.io.huangnosimp.tasks.SetupMcVersion

data class McSetupPipelineResult(
    val setupMcVersion: TaskProvider<SetupMcVersion>,
    val libsDir: Provider<Directory>,
)
