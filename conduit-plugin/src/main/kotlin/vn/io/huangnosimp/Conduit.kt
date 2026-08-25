package vn.io.huangnosimp

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import vn.io.huangnosimp.constants.BUNDLER_JAR
import vn.io.huangnosimp.constants.CONDUIT_CACHE_DIR
import vn.io.huangnosimp.constants.DECOMPILED_JAR
import vn.io.huangnosimp.constants.LIBS_DIR
import vn.io.huangnosimp.constants.MC_MANIFEST
import vn.io.huangnosimp.constants.RESOURCES_DIR
import vn.io.huangnosimp.constants.SERVER_JAR
import vn.io.huangnosimp.constants.SOURCES_DIR
import vn.io.huangnosimp.constants.VERSION_MANIFEST
import vn.io.huangnosimp.extension.ConduitExtension
import vn.io.huangnosimp.tasks.DecompileServerJar
import vn.io.huangnosimp.tasks.DownloadBundlerJar
import vn.io.huangnosimp.tasks.DownloadMcManifest
import vn.io.huangnosimp.tasks.DownloadVersionManifest
import vn.io.huangnosimp.tasks.ExtractBundlerJar
import vn.io.huangnosimp.tasks.SetupMcVersion

class Conduit : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create("conduit", ConduitExtension::class.java)
        val downloadMcManifest =
            project.tasks.register("downloadMcManifest", DownloadMcManifest::class.java) {
                it.mcManifest.set(project.layout.projectDirectory.file(CONDUIT_CACHE_DIR + MC_MANIFEST))
            }
        project.afterEvaluate {
            if (ext.mcBaseVersion.isPresent) {
                project.registerMcSetupPipeline(
                    type = "Base",
                    version = ext.mcBaseVersion,
                    mcManifest =
                        downloadMcManifest.flatMap {
                            it.mcManifest
                        },
                )
            }
            if (ext.mcUpdateVersion.isPresent) {
                project.registerMcSetupPipeline(
                    type = "Update",
                    version = ext.mcUpdateVersion,
                    mcManifest =
                        downloadMcManifest.flatMap {
                            it.mcManifest
                        },
                )
            }
        }
    }

    private fun Project.registerMcSetupPipeline(
        version: Provider<String>,
        mcManifest: Provider<RegularFile>,
        type: String,
    ) {
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
    }
}
