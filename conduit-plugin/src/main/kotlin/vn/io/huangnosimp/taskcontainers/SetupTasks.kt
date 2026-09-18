package vn.io.huangnosimp.taskcontainers

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import vn.io.huangnosimp.constants.APPLY_MACHE_PATCHES
import vn.io.huangnosimp.constants.BUNDLER_JAR
import vn.io.huangnosimp.constants.DECOMPILED_JAR
import vn.io.huangnosimp.constants.DECOMPILE_SERVER_JAR
import vn.io.huangnosimp.constants.DOWNLOAD_BUNDLE_JAR
import vn.io.huangnosimp.constants.DOWNLOAD_VERSION_MANIFEST
import vn.io.huangnosimp.constants.EXTRACT_BUNDLE_JAR
import vn.io.huangnosimp.constants.EXTRACT_TO_WORKSPACE
import vn.io.huangnosimp.constants.LIBS_DIR
import vn.io.huangnosimp.constants.MACHE_CODEBOOK_CONFIG
import vn.io.huangnosimp.constants.MACHE_CONSTANTS_CONFIG
import vn.io.huangnosimp.constants.MACHE_DECOMPILER_CONFIG
import vn.io.huangnosimp.constants.MACHE_PARAM_MAPPINGS_CONFIG
import vn.io.huangnosimp.constants.MACHE_REMAPPER_CONFIG
import vn.io.huangnosimp.constants.MINECRAFT_DEPENDENCIES_CONFIG
import vn.io.huangnosimp.constants.PATCHED_JAR
import vn.io.huangnosimp.constants.REMAPPED_JAR
import vn.io.huangnosimp.constants.REMAP_SERVER_JAR
import vn.io.huangnosimp.constants.RESOURCES_DIR
import vn.io.huangnosimp.constants.SERVER_JAR
import vn.io.huangnosimp.constants.SERVER_MAPPING
import vn.io.huangnosimp.constants.SOURCES_DIR
import vn.io.huangnosimp.constants.VERSION_MANIFEST
import vn.io.huangnosimp.data.mache.MacheMetaData
import vn.io.huangnosimp.tasks.ApplyMachePatches
import vn.io.huangnosimp.tasks.DecompileServerJar
import vn.io.huangnosimp.tasks.DownloadBundlerJar
import vn.io.huangnosimp.tasks.DownloadMcManifest
import vn.io.huangnosimp.tasks.DownloadVersionManifest
import vn.io.huangnosimp.tasks.ExtractBundlerJar
import vn.io.huangnosimp.tasks.ExtractToWorkspace
import vn.io.huangnosimp.tasks.RemapServerJar
import kotlin.jvm.java

class SetupTasks(
    project: Project,
    downloadMcManifest: TaskProvider<DownloadMcManifest>,
    mcVersion: Property<String>,
    macheConfig: Configuration,
    macheProvider: Property<MacheMetaData>,
) {
    val downloadVersionManifest =
        project.tasks.register(DOWNLOAD_VERSION_MANIFEST, DownloadVersionManifest::class.java) {
            it.mcManifest.set(downloadMcManifest.flatMap { task -> task.mcManifest })
            it.mcVersion.set(mcVersion)
            it.versionManifest.set(project.layout.projectDirectory.file(VERSION_MANIFEST))
        }
    val downloadBundlerJar =
        project.tasks.register(DOWNLOAD_BUNDLE_JAR, DownloadBundlerJar::class.java) {
            it.versionManifest.set(downloadVersionManifest.flatMap { task -> task.versionManifest })
            it.bundlerJar.set(project.layout.projectDirectory.file(BUNDLER_JAR))
            it.serverMapping.set(project.layout.projectDirectory.file(SERVER_MAPPING))
        }
    val extractBundlerJar =
        project.tasks.register(EXTRACT_BUNDLE_JAR, ExtractBundlerJar::class.java) {
            it.bundleJar.set(downloadBundlerJar.flatMap { task -> task.bundlerJar })
            it.libsDir.set(project.layout.projectDirectory.dir(LIBS_DIR))
            it.serverJar.set(project.layout.projectDirectory.file(SERVER_JAR))
        }
    val remapServerJar =
        project.tasks.register(REMAP_SERVER_JAR, RemapServerJar::class.java) {
            it.serverJar.set(extractBundlerJar.flatMap { task -> task.serverJar })
            it.codebookArgs.set(project.provider { macheProvider.get().remapperArgs })
            it.codebookClasspath.from(project.configurations.named(MACHE_CODEBOOK_CONFIG))
            it.minecraftClasspath.from(project.configurations.named(MINECRAFT_DEPENDENCIES_CONFIG))
            it.constants.from(project.configurations.named(MACHE_CONSTANTS_CONFIG))
            it.remapperClasspath.from(project.configurations.named(MACHE_REMAPPER_CONFIG))
            it.serverMapping.from(
                downloadBundlerJar.flatMap { task -> task.serverMapping }.map { file ->
                    if (file.asFile.exists()) {
                        listOf(file.asFile)
                    } else {
                        emptyList()
                    }
                },
            )
            it.paramMappings.from(project.configurations.named(MACHE_PARAM_MAPPINGS_CONFIG))
            it.remappedServerJar.set(project.layout.projectDirectory.file(REMAPPED_JAR))

            it.javaLauncher.set(
                project.extensions.getByType(JavaToolchainService::class.java).launcherFor { spec ->
                    spec.languageVersion.set(
                        JavaLanguageVersion.of(25),
                    )
                },
            )
        }
    val decompileServerJar =
        project.tasks.register(DECOMPILE_SERVER_JAR, DecompileServerJar::class.java) {
            it.remappedServerJar.set(remapServerJar.flatMap { task -> task.remappedServerJar })
            it.decompilerArgs.set(project.provider { macheProvider.get().decompilerArgs })
            it.decompilerClasspath.from(project.configurations.named(MACHE_DECOMPILER_CONFIG))
            it.minecraftClasspath.from(project.configurations.named(MINECRAFT_DEPENDENCIES_CONFIG))
            it.decompiledServerJar.set(project.layout.projectDirectory.file(DECOMPILED_JAR))

            it.javaLauncher.set(
                project.extensions.getByType(JavaToolchainService::class.java).launcherFor { spec ->
                    spec.languageVersion.set(
                        JavaLanguageVersion.of(25),
                    )
                },
            )
        }
    val applyMachePatches =
        project.tasks.register(APPLY_MACHE_PATCHES, ApplyMachePatches::class.java) {
            it.decompiledServerJar.set(decompileServerJar.flatMap { task -> task.decompiledServerJar })
            it.mache.fileProvider(project.provider { macheConfig.singleFile })
            it.patchedServerJar.set(project.layout.projectDirectory.file(PATCHED_JAR))
        }
    val extractToWorkspace =
        project.tasks.register(EXTRACT_TO_WORKSPACE, ExtractToWorkspace::class.java) {
            it.patchedJar.set(applyMachePatches.flatMap { task -> task.patchedServerJar })
            it.remappedJar.set(remapServerJar.flatMap { task -> task.remappedServerJar })
            it.sourceDir.set(project.layout.projectDirectory.dir(SOURCES_DIR))
            it.resourceDir.set(project.layout.projectDirectory.dir(RESOURCES_DIR))
        }
}
