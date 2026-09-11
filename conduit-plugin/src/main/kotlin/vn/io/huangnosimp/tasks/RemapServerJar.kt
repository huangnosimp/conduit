package vn.io.huangnosimp.tasks

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.CompileClasspath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
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
    @get:PathSensitive(PathSensitivity.RELATIVE)
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
    abstract val remappedJar: RegularFileProperty

    @get:Classpath
    abstract val remapperClasspath: ConfigurableFileCollection?

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val serverMapping: RegularFileProperty?

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val paramMappings: ConfigurableFileCollection?

    init {
        group = CONDUIT
    }

    @TaskAction
    fun run() {
        mainClass.set(JarFile(codebookClasspath.singleFile).manifest.mainAttributes.getValue("Main-Class"))
        classpath = codebookClasspath
        jvmArgs = listOf("-Xmx2G")
        val args = mutableListOf<String>()

        codebookArgs.get().forEach { arg ->
            args +=
                arg
                    .replace(Regex("\\{tempDir}")) { temporaryDir.toPath().absolutePathString() }
                    .replace(Regex("\\{remapperFile}")) {
                        remapperClasspath?.singleFile?.absolutePath ?: error("{remapperFile} in args, but no remapperClasspath provided")
                    }.replace(Regex("\\{mappingsFile}")) {
                        serverMapping
                            ?.get()
                            ?.asFile
                            ?.toPath()
                            ?.absolutePathString()
                            ?: error("{mappingsFile} in args, but no serverMappings provided")
                    }.replace(Regex("\\{paramsFile}")) {
                        paramMappings?.singleFile?.toPath()?.absolutePathString()
                            ?: error("{paramsFile} in args, but no paramMappings provided")
                    }.replace(Regex("\\{constantsFile}")) {
                        constants.files
                            .singleOrNull()
                            ?.toPath()
                            ?.absolutePathString()
                            ?: error("{constantsFile} in args, but no constants provided")
                    }.replace(Regex("\\{output}")) {
                        remappedJar
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
    }
}
