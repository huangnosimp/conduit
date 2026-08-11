package vn.io.huangnosimp

import org.gradle.api.Plugin
import org.gradle.api.Project
import vn.io.huangnosimp.extension.ConduitExtension
import vn.io.huangnosimp.tasks.DownloadMcManifest
import vn.io.huangnosimp.tasks.DownloadServerJar
import vn.io.huangnosimp.tasks.DownloadVersionManifest
import vn.io.huangnosimp.tasks.ExtractServerJar

class Conduit : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create("conduit", ConduitExtension::class.java)
        val downloadMcManifest = project.tasks.register("downloadMcManifest", DownloadMcManifest::class.java) {
            it.group = "conduit"
            it.outputFile.set(project.layout.buildDirectory.file("conduit/manifest/mcManifest.json"))
        }
        val downloadVersionManifest = project.tasks.register("downloadVersionManifest", DownloadVersionManifest::class.java) {
            it.group = "conduit"
            it.mcVersion.set(ext.mcVersion)
            it.inputFile.set(downloadMcManifest.flatMap {
                task -> task.outputFile
            })
            it.outputFile.set(project.layout.buildDirectory.file("conduit/manifest/versionManifest.json"))
        }
        val downloadServerJar = project.tasks.register("downloadServerJar", DownloadServerJar::class.java) {
            it.group = "conduit"
            it.inputFile.set(downloadVersionManifest.flatMap {
                task -> task.outputFile
            })
            it.outputFile.set(project.layout.buildDirectory.file("conduit/server.jar"))
        }
        val extractServerJar = project.tasks.register("extractServerJar", ExtractServerJar::class.java) {
            it.group = "conduit"
            it.inputFile.set(downloadServerJar.flatMap {
                task -> task.outputFile
            })
            it.outputDirectory.set(project.layout.buildDirectory.dir("conduit/bundle"))
        }
    }
}