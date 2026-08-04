package vn.io.huangnosimp

import org.gradle.api.Plugin
import org.gradle.api.Project
import vn.io.huangnosimp.tasks.DownloadMcManifest

class ConduitPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("DownloadMcManifest", DownloadMcManifest::class.java) {
        }
    }
}