package vn.io.huangnosimp.tasks

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import vn.io.huangnosimp.constants.CONDUIT

@CacheableTask
abstract class DecompileServerJar : JavaExec() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val remappedServerJar: RegularFileProperty

    @get:Input
    abstract val decompilerArgs: ListProperty<String>

    @get:Classpath
    abstract val decompilerClasspath: ConfigurableFileCollection

    @get:Classpath
    abstract val minecraftClasspath: ConfigurableFileCollection

    @get:OutputFile
    abstract val decompiledServerJar: RegularFileProperty

    @get:Input
    abstract val maxHeapSize: Property<String>

    init {
        group = CONDUIT
        maxHeapSize.convention("4G")
    }

    @TaskAction
    fun run() {

    }
}
