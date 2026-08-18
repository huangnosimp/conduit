package vn.io.huangnosimp

import org.gradle.api.Plugin
import org.gradle.api.Project
import vn.io.huangnosimp.extension.ConduitExtension
import vn.io.huangnosimp.tasks.DecompileServerJar
import vn.io.huangnosimp.tasks.DownloadBundlerJar
import vn.io.huangnosimp.tasks.DownloadMcManifest
import vn.io.huangnosimp.tasks.DownloadVersionManifest
import vn.io.huangnosimp.tasks.ExtractBundlerJar

class Conduit : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create("conduit", ConduitExtension::class.java)
        val downloadMcManifest =
            project.tasks.register("downloadMcManifest", DownloadMcManifest::class.java) {
                it.group = "conduit"
                it.mcManifest.set(project.layout.buildDirectory.file("conduit/manifest/mcManifest.json"))
            }
        val downloadVersionManifest =
            project.tasks.register("downloadVersionManifest", DownloadVersionManifest::class.java) {
                it.group = "conduit"
                it.mcVersion.set(ext.mcVersion)
                it.mcManifest.set(
                    downloadMcManifest.flatMap { task ->
                        task.mcManifest
                    },
                )
                it.versionManifest.set(project.layout.buildDirectory.file("conduit/manifest/versionManifest.json"))
            }
        val downloadBundlerJar =
            project.tasks.register("downloadBundlerJar", DownloadBundlerJar::class.java) {
                it.group = "conduit"
                it.versionManifest.set(
                    downloadVersionManifest.flatMap { task ->
                        task.versionManifest
                    },
                )
                it.bundlerJar.set(project.layout.buildDirectory.file("conduit/jars/bundler.jar"))
            }
        val extractBundlerJar =
            project.tasks.register("extractBundlerJar", ExtractBundlerJar::class.java) {
                it.group = "conduit"
                it.bundleJar.set(
                    downloadBundlerJar.flatMap { task ->
                        task.bundlerJar
                    },
                )
                it.serverJar.set(project.layout.buildDirectory.file("conduit/jars/server.jar"))
                it.libsDir.set(project.layout.buildDirectory.dir("conduit/libs"))
            }
        val decompileServerJar =
            project.tasks.register("decompileServerJar", DecompileServerJar::class.java) {
                it.group = "conduit"
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
                it.outputJar.set(project.layout.buildDirectory.file("conduit/jars/decompiled_server.jar"))
            }
    }
}
