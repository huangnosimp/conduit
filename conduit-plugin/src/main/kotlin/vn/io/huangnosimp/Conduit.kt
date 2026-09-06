package vn.io.huangnosimp

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import vn.io.huangnosimp.constants.CONDUIT_CACHE_DIR
import vn.io.huangnosimp.constants.MACHE_JSON
import vn.io.huangnosimp.constants.MACHE_PATCHES_DIR
import vn.io.huangnosimp.constants.MC_MANIFEST
import vn.io.huangnosimp.constants.PAPERMC_REPOSITORY_URL
import vn.io.huangnosimp.extension.ConduitExtension
import vn.io.huangnosimp.tasks.CleanConduitCache
import vn.io.huangnosimp.tasks.DownloadMcManifest
import vn.io.huangnosimp.tasks.ExtractMache
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
        val downloadMcManifest =
            project.tasks.register("downloadMcManifest", DownloadMcManifest::class.java) {
                it.mcManifest.set(project.layout.projectDirectory.file(CONDUIT_CACHE_DIR + MC_MANIFEST))
            }
        project.tasks.register("cleanConduitCache", CleanConduitCache::class.java) {
            it.cacheDir.set(project.layout.projectDirectory.dir(CONDUIT_CACHE_DIR))
        }

        project.tasks.register("extractMache", ExtractMache::class.java) {
            it.mache.fileProvider(project.provider { macheConfig.singleFile })
            it.macheJson.set(project.layout.projectDirectory.file(MACHE_JSON))
            it.patchesDir.set(project.layout.projectDirectory.dir(MACHE_PATCHES_DIR))
        }
    }
}
