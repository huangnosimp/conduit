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
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import vn.io.huangnosimp.constants.CONDUIT
import java.util.jar.JarFile
import kotlin.io.path.absolutePathString

@CacheableTask
abstract class RemapServerJar : JavaExec() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val serverJar: RegularFileProperty

    @get:Input
    abstract val codebookArgs: ListProperty<String>

    @get:Classpath
    abstract val codebookClasspath: ConfigurableFileCollection

    @get:CompileClasspath
    abstract val minecraftClasspath: ConfigurableFileCollection

    @get:Classpath
    abstract val constants: ConfigurableFileCollection

    @get:OutputFile
    abstract val remappedServerJar: RegularFileProperty

    @get:Optional
    @get:Classpath
    abstract val remapperClasspath: ConfigurableFileCollection

    @get:Optional
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val serverMapping: ConfigurableFileCollection

    @get:Optional
    @get:Classpath
    abstract val paramMappings: ConfigurableFileCollection

    @get:Input
    abstract val memory: Property<String>

    init {
        group = CONDUIT
        memory.convention("2G")
    }

    @TaskAction
    fun run() {
        remappedServerJar.get().asFile.delete()
        mainClass.set(providerFactory.provider { JarFile(codebookClasspath.singleFile).manifest.mainAttributes.getValue("Main-Class") })
        classpath = codebookClasspath
        jvmArgs = listOf("-Xmx${memory.get()}")
        val args = mutableListOf<String>()

        codebookArgs.get().forEach { arg ->
            args +=
                arg
                    .replace(Regex("\\{tempDir}")) { temporaryDir.toPath().absolutePathString() }
                    .replace(Regex("\\{remapperFile}")) {
                        remapperClasspath.singleFile.absolutePath ?: error("{remapperFile} in args, but no remapperClasspath provided")
                    }.replace(Regex("\\{mappingsFile}")) {
                        serverMapping
                            .files
                            .singleOrNull()
                            ?.toPath()
                            ?.absolutePathString()
                            ?: error("{mappingsFile} in args, but no serverMappings provided")
                    }.replace(Regex("\\{paramsFile}")) {
                        paramMappings.singleFile.toPath().absolutePathString()
                    }.replace(Regex("\\{constantsFile}")) {
                        constants.files
                            .singleOrNull()
                            ?.toPath()
                            ?.absolutePathString()
                            ?: error("{constantsFile} in args, but no constants provided")
                    }.replace(Regex("\\{output}")) {
                        remappedServerJar
                            .get()
                            .asFile
                            .toPath()
                            .absolutePathString()
                    }.replace(Regex("\\{input}")) {
                        serverJar
                            .get()
                            .asFile
                            .toPath()
                            .absolutePathString()
                    }.replace(
                        Regex("\\{inputClasspath}"),
                    ) { minecraftClasspath.files.map { it.toPath() }.joinToString(":") { it.absolutePathString() } }
        }

        setArgs(args)
        standardOutput =
            temporaryDir
                .toPath()
                .resolve("${remappedServerJar.get().asFile.name}.log")
                .toFile()
                .outputStream()
        errorOutput = standardOutput
    }
}
