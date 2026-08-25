package vn.io.huangnosimp.utils

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import vn.io.huangnosimp.constants.BUNDLER_JAR
import vn.io.huangnosimp.constants.CONDUIT_CACHE_DIR
import vn.io.huangnosimp.constants.DECOMPILED_JAR
import vn.io.huangnosimp.constants.LIBS_DIR
import vn.io.huangnosimp.constants.RESOURCES_DIR
import vn.io.huangnosimp.constants.SERVER_JAR
import vn.io.huangnosimp.constants.SOURCES
import vn.io.huangnosimp.constants.SOURCES_DIR
import vn.io.huangnosimp.constants.VERSION_MANIFEST
import vn.io.huangnosimp.constants.WORKSPACE_RESOURCES_DIR
import vn.io.huangnosimp.constants.WORKSPACE_SOURCES_DIR
import vn.io.huangnosimp.data.McSetupPipelineResult
import vn.io.huangnosimp.tasks.DecompileServerJar
import vn.io.huangnosimp.tasks.DownloadBundlerJar
import vn.io.huangnosimp.tasks.DownloadVersionManifest
import vn.io.huangnosimp.tasks.ExtractBundlerJar
import vn.io.huangnosimp.tasks.SetupMcVersion
import vn.io.huangnosimp.tasks.SetupWorkspace

fun Project.registerMcSetupPipeline(
    version: Provider<String>,
    mcManifest: Provider<RegularFile>,
    type: String,
): McSetupPipelineResult {
    val downloadVersionManifest =
        project.tasks.register("download${type}VersionManifest", DownloadVersionManifest::class.java) {
            it.mcVersion.set(version)
            it.mcManifest.set(
                mcManifest,
            )
            it.versionManifest.set(
                project.layout.projectDirectory.file(CONDUIT_CACHE_DIR + "${type.lowercase()}/" + VERSION_MANIFEST),
            )
        }
    val downloadBundlerJar =
        project.tasks.register("download${type}BundlerJar", DownloadBundlerJar::class.java) {
            it.versionManifest.set(
                downloadVersionManifest.flatMap { task ->
                    task.versionManifest
                },
            )
            it.bundlerJar.set(project.layout.projectDirectory.file(CONDUIT_CACHE_DIR + "${type.lowercase()}/" + BUNDLER_JAR))
        }
    val extractBundlerJar =
        project.tasks.register("extract${type}BundlerJar", ExtractBundlerJar::class.java) {
            it.bundleJar.set(
                downloadBundlerJar.flatMap { task ->
                    task.bundlerJar
                },
            )
            it.serverJar.set(project.layout.projectDirectory.file(CONDUIT_CACHE_DIR + "${type.lowercase()}/" + SERVER_JAR))
            it.libsDir.set(project.layout.projectDirectory.dir(CONDUIT_CACHE_DIR + "${type.lowercase()}/" + LIBS_DIR))
        }
    val decompileServerJar =
        project.tasks.register("decompile${type}ServerJar", DecompileServerJar::class.java) {
            it.serverJar.set(
                extractBundlerJar.flatMap { task ->
                    task.serverJar
                },
            )
            it.libsDir.set(
                extractBundlerJar.flatMap { task ->
                    task.libsDir
                },
            )
            it.outputJar.set(project.layout.projectDirectory.file(CONDUIT_CACHE_DIR + "${type.lowercase()}/" + DECOMPILED_JAR))
        }
    val setupMcVersion =
        project.tasks.register("setupMc$type", SetupMcVersion::class.java) {
            it.decompiledJar.set(
                decompileServerJar.flatMap { task ->
                    task.outputJar
                },
            )
            it.mcBaseVersion.set(version)
            it.sourcesDir.set(project.layout.projectDirectory.dir(CONDUIT_CACHE_DIR + "${type.lowercase()}/" + SOURCES_DIR))
            it.resourcesDir.set(project.layout.projectDirectory.dir(CONDUIT_CACHE_DIR + "${type.lowercase()}/" + RESOURCES_DIR))
        }
    return McSetupPipelineResult(
        setupMcVersion = setupMcVersion,
        libsDir = extractBundlerJar.flatMap { it.libsDir },
    )
}

fun Project.registerPatchingPipeline(
    inputDir: Provider<Directory>,
    type: String,
) {
    val setupWorkspace =
        project.tasks.register("setup${type}Workspace", SetupWorkspace::class.java) {
            it.baseDir.set(inputDir)
            if (type == SOURCES) {
                it.workspaceDir.set(project.layout.projectDirectory.dir(WORKSPACE_SOURCES_DIR))
            } else {
                it.workspaceDir.set(project.layout.projectDirectory.dir(WORKSPACE_RESOURCES_DIR))
            }
        }
    val sourceSets =
        project.extensions
            .getByType(JavaPluginExtension::class.java)
            .sourceSets
            .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
    if (type == SOURCES) {
        sourceSets.java.srcDirs(
            setupWorkspace.flatMap { task ->
                task.workspaceDir
            },
        )
        sourceSets.java.exclude(".git/**")
    } else {
        sourceSets.resources.srcDirs(
            setupWorkspace.flatMap { task ->
                task.workspaceDir
            },
        )
        sourceSets.resources.exclude(".git/**")
    }
}
