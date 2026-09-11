package vn.io.huangnosimp.taskcontainers

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import vn.io.huangnosimp.constants.BUNDLER_JAR
import vn.io.huangnosimp.constants.DOWNLOAD_BUNDLE_JAR
import vn.io.huangnosimp.constants.DOWNLOAD_VERSION_MANIFEST
import vn.io.huangnosimp.constants.EXTRACT_BUNDLE_JAR
import vn.io.huangnosimp.constants.EXTRACT_MACHE
import vn.io.huangnosimp.constants.LIBS_DIR
import vn.io.huangnosimp.constants.MACHE_CODEBOOK_CONFIG
import vn.io.huangnosimp.constants.MACHE_CONSTANTS_CONFIG
import vn.io.huangnosimp.constants.MACHE_JSON
import vn.io.huangnosimp.constants.MACHE_PARAM_MAPPINGS_CONFIG
import vn.io.huangnosimp.constants.MACHE_PATCHES_DIR
import vn.io.huangnosimp.constants.MACHE_REMAPPER_CONFIG
import vn.io.huangnosimp.constants.REMAPPED_JAR
import vn.io.huangnosimp.constants.REMAP_SERVER_JAR
import vn.io.huangnosimp.constants.SERVER_JAR
import vn.io.huangnosimp.constants.SERVER_MAPPING
import vn.io.huangnosimp.constants.VERSION_MANIFEST
import vn.io.huangnosimp.data.mache.MacheMetaData
import vn.io.huangnosimp.tasks.DownloadBundlerJar
import vn.io.huangnosimp.tasks.DownloadMcManifest
import vn.io.huangnosimp.tasks.DownloadVersionManifest
import vn.io.huangnosimp.tasks.ExtractBundlerJar
import vn.io.huangnosimp.tasks.ExtractMache
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
    val extractMache =
        project.tasks.register(EXTRACT_MACHE, ExtractMache::class.java) {
            it.mache.fileProvider(project.provider { macheConfig.singleFile })
            it.macheJson.set(project.layout.projectDirectory.file(MACHE_JSON))
            it.patchesDir.set(project.layout.projectDirectory.dir(MACHE_PATCHES_DIR))
        }
    val remapServerJar =
        project.tasks.register(REMAP_SERVER_JAR, RemapServerJar::class.java) {
            it.serverJar.set(extractBundlerJar.flatMap { task -> task.serverJar })
            it.codebookArgs.set(project.provider { macheProvider.get().remapperArgs })
            it.codebookClasspath.from(project.configurations.named(MACHE_CODEBOOK_CONFIG))
            it.minecraftClasspath.from(extractBundlerJar.flatMap { task -> task.libsDir }.map { dir -> dir.asFileTree })
            it.constants.from(project.configurations.named(MACHE_CONSTANTS_CONFIG))
            it.remapperClasspath?.from(project.configurations.named(MACHE_REMAPPER_CONFIG))
            it.serverMapping?.set(downloadBundlerJar.flatMap { task -> task.serverMapping })
            it.paramMappings?.from(project.configurations.named(MACHE_PARAM_MAPPINGS_CONFIG))
            it.remappedJar.set(project.layout.projectDirectory.file(REMAPPED_JAR))
        }
}
