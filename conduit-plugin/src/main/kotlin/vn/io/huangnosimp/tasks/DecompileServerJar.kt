package vn.io.huangnosimp.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import org.jetbrains.java.decompiler.api.Decompiler
import org.jetbrains.java.decompiler.main.decompiler.DirectoryResultSaver
import javax.inject.Inject

@CacheableTask
abstract class DecompileServerJar @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val serverJar: RegularFileProperty

    @get:OutputDirectory
    abstract val sourceDir: DirectoryProperty

    @get:Input
    abstract val workerMaxHeapSize: Property<String>

    @get:Input
    abstract val workerMaxMetaspaceSize: Property<String>

    init {
        workerMaxHeapSize.convention("2g")
        workerMaxMetaspaceSize.convention("512m")
    }

    @TaskAction
    fun run() {
        val workQueue = workerExecutor.processIsolation { workerSpec ->
            workerSpec.forkOptions { forkOptions ->
                forkOptions.maxHeapSize = workerMaxHeapSize.get()
                forkOptions.jvmArgs("-XX:MaxMetaspaceSize=${workerMaxMetaspaceSize.get()}")
            }
        }
        workQueue.submit(DecompileServerJarWorkAction::class.java) {
            it.serverJar.set(serverJar)
            it.sourceDir.set(sourceDir)
        }
        workQueue.await()
    }
}

interface DecompileServerJarParameters : WorkParameters {
    val serverJar: RegularFileProperty
    val sourceDir: DirectoryProperty
}

abstract class DecompileServerJarWorkAction : WorkAction<DecompileServerJarParameters> {
    override fun execute() {
        val decompiler = Decompiler.builder()
            .inputs(parameters.serverJar.get().asFile)
            .output(DirectoryResultSaver(parameters.sourceDir.get().asFile))
            .build()
        decompiler.decompile()
    }
}
