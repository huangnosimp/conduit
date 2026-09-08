package vn.io.huangnosimp

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import vn.io.huangnosimp.constants.DOWNLOAD_MC_MANIFEST
import vn.io.huangnosimp.constants.MC_MANIFEST
import vn.io.huangnosimp.constants.PAPERMC_REPOSITORY_URL
import vn.io.huangnosimp.extension.ConduitExtension
import vn.io.huangnosimp.taskcontainers.SetupTasks
import vn.io.huangnosimp.tasks.DownloadMcManifest
import vn.io.huangnosimp.utils.resolveLatestMacheVersion

class Conduit : Plugin<Project> {
    override fun apply(project: Project) {
        // Extension
        val ext = project.extensions.create("conduit", ConduitExtension::class.java)
        if (!project.plugins.hasPlugin("java")) {
            project.plugins.apply("java")
        }

        // project config
        project.repositories.apply {
            maven { it.url = project.uri(PAPERMC_REPOSITORY_URL) }
        }

        val macheConfig =
            project.configurations.detachedConfiguration().apply {
                isTransitive = false
            }

        macheConfig.dependencies.addLater(
            ext.mcBaseVersion.map { mcVersion ->
                val version = resolveLatestMacheVersion(mcVersion)
                project.dependencies.create("io.papermc:mache:$version@zip")
            },
        )

        val sourceSets =
            project.extensions
                .getByType(JavaPluginExtension::class.java)
                .sourceSets
                .getByName(SourceSet.MAIN_SOURCE_SET_NAME)

        // Task register
        val downloadMcManifest = project.tasks.register(DOWNLOAD_MC_MANIFEST, DownloadMcManifest::class.java) {
            it.mcManifest.set(project.layout.projectDirectory.file(MC_MANIFEST))
        }
        val setupTasks = SetupTasks(project, downloadMcManifest, ext.mcBaseVersion, macheConfig)
    }
}
