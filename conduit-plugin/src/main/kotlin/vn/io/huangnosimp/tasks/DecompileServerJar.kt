package vn.io.huangnosimp.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import vn.io.huangnosimp.worker.DecompileServerJarWorkAction
import javax.inject.Inject

@CacheableTask
abstract class DecompileServerJar
    @Inject
    constructor(
        private val workerExecutor: WorkerExecutor,
    ) : DefaultTask() {
        @get:Classpath
        abstract val serverJar: RegularFileProperty

        @get:InputDirectory
        @get:PathSensitive(PathSensitivity.RELATIVE)
        abstract val libsDir: DirectoryProperty

        @get:OutputFile
        abstract val outputJar: RegularFileProperty

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
            val workQueue =
                workerExecutor.processIsolation { workerSpec ->
                    workerSpec.forkOptions { forkOptions ->
                        forkOptions.maxHeapSize = workerMaxHeapSize.get()
                        forkOptions.jvmArgs("-XX:MaxMetaspaceSize=${workerMaxMetaspaceSize.get()}")
                    }
                }
            workQueue.submit(DecompileServerJarWorkAction::class.java) {
                it.serverJar.set(serverJar)
                it.libsDir.set(libsDir)
                it.outputJar.set(outputJar)
            }
            workQueue.await()
        }
    }
