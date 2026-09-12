package vn.io.huangnosimp.tasks

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.CompileClasspath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import vn.io.huangnosimp.constants.CONDUIT
import java.util.jar.JarFile
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText

@CacheableTask
abstract class DecompileServerJar : JavaExec() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val remappedServerJar: RegularFileProperty

    @get:Input
    abstract val decompilerArgs: ListProperty<String>

    @get:Classpath
    abstract val decompilerClasspath: ConfigurableFileCollection

    @get:CompileClasspath
    abstract val minecraftClasspath: ConfigurableFileCollection

    @get:OutputFile
    abstract val decompiledServerJar: RegularFileProperty

    @get:Input
    abstract val memory: Property<String>

    init {
        group = CONDUIT
        memory.convention("4G")
    }

    @TaskAction
    fun run() {
        decompiledServerJar.get().asFile.delete()
        val cfgFile = temporaryDir.toPath().resolve("${decompiledServerJar.get().asFile.name}.cfg")
        val cfgText =
            buildString {
                for (file in minecraftClasspath.files.map { it.toPath() }) {
                    append("-e=")
                    append(file.absolutePathString())
                    append(System.lineSeparator())
                }
            }
        cfgFile.writeText(cfgText)

        val args = mutableListOf<String>()
        args += decompilerArgs.get()
        args += "-cfg"
        args += cfgFile.absolutePathString()
        args +=
            remappedServerJar
                .get()
                .asFile
                .toPath()
                .absolutePathString()
        args +=
            decompiledServerJar
                .get()
                .asFile
                .toPath()
                .absolutePathString()

        mainClass.set(JarFile(decompilerClasspath.singleFile).manifest.mainAttributes.getValue("Main-Class"))
        classpath = decompilerClasspath
        jvmArgs = listOf("-Xmx${memory.get()}")
        setArgs(args)
        println(args)
        standardOutput =
            temporaryDir
                .toPath()
                .resolve("${decompiledServerJar.get().asFile.name}.log")
                .toFile()
                .outputStream()
        errorOutput = standardOutput
    }
}
