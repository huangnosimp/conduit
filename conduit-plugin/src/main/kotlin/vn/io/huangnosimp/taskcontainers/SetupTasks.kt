package vn.io.huangnosimp.taskcontainers

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import vn.io.huangnosimp.constants.BASE_DIR
import vn.io.huangnosimp.constants.BUNDLER_JAR
import vn.io.huangnosimp.constants.CONDUIT_CACHE_DIR
import vn.io.huangnosimp.constants.DOWNLOAD_BUNDLE_JAR
import vn.io.huangnosimp.constants.DOWNLOAD_VERSION_MANIFEST
import vn.io.huangnosimp.constants.EXTRACT_BUNDLE_JAR
import vn.io.huangnosimp.constants.EXTRACT_MACHE
import vn.io.huangnosimp.constants.LIBS_DIR
import vn.io.huangnosimp.constants.MACHE_DIR
import vn.io.huangnosimp.constants.MACHE_JSON
import vn.io.huangnosimp.constants.MACHE_PATCHES_DIR
import vn.io.huangnosimp.constants.SERVER_JAR
import vn.io.huangnosimp.constants.VERSION_MANIFEST
import vn.io.huangnosimp.tasks.DownloadBundlerJar
import vn.io.huangnosimp.tasks.DownloadMcManifest
import vn.io.huangnosimp.tasks.DownloadVersionManifest
import vn.io.huangnosimp.tasks.ExtractBundlerJar
import vn.io.huangnosimp.tasks.ExtractMache
import kotlin.jvm.java

class SetupTasks(
    project: Project,
    downloadMcManifest: TaskProvider<DownloadMcManifest>,
    mcVersion: Property<String>,
    macheConfig: Configuration,
) {
    val downloadVersionManifest =
        project.tasks.register(DOWNLOAD_VERSION_MANIFEST, DownloadVersionManifest::class.java) {
            it.mcManifest.set(downloadMcManifest.flatMap { task -> task.mcManifest })
            it.mcVersion.set(mcVersion)
            it.versionManifest.set(project.layout.projectDirectory.file(CONDUIT_CACHE_DIR + BASE_DIR + VERSION_MANIFEST))
        }
    val downloadBundlerJar =
        project.tasks.register(DOWNLOAD_BUNDLE_JAR, DownloadBundlerJar::class.java) {
            it.versionManifest.set(downloadVersionManifest.flatMap { task -> task.versionManifest })
            it.bundlerJar.set(project.layout.projectDirectory.file(CONDUIT_CACHE_DIR + BASE_DIR + BUNDLER_JAR))
        }
    val extractBundlerJar =
        project.tasks.register(EXTRACT_BUNDLE_JAR, ExtractBundlerJar::class.java) {
            it.bundleJar.set(downloadBundlerJar.flatMap { task -> task.bundlerJar })
            it.libsDir.set(project.layout.projectDirectory.dir(CONDUIT_CACHE_DIR + BASE_DIR + LIBS_DIR))
            it.serverJar.set(project.layout.projectDirectory.file(CONDUIT_CACHE_DIR + BASE_DIR + SERVER_JAR))
        }
    val extractMache =
        project.tasks.register(EXTRACT_MACHE, ExtractMache::class.java) {
            it.mache.fileProvider(project.provider { macheConfig.singleFile })
            it.macheJson.set(project.layout.projectDirectory.file(CONDUIT_CACHE_DIR + BASE_DIR + MACHE_DIR + MACHE_JSON))
            it.patchesDir.set(project.layout.projectDirectory.dir(CONDUIT_CACHE_DIR + BASE_DIR + MACHE_DIR + MACHE_PATCHES_DIR))
        }
}
