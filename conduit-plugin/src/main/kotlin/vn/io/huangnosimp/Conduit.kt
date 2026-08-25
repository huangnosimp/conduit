package vn.io.huangnosimp

import org.gradle.api.Plugin
import org.gradle.api.Project
import vn.io.huangnosimp.constants.BASE
import vn.io.huangnosimp.constants.CONDUIT_CACHE_DIR
import vn.io.huangnosimp.constants.MC_MANIFEST
import vn.io.huangnosimp.constants.RESOURCES
import vn.io.huangnosimp.constants.SOURCES
import vn.io.huangnosimp.constants.UPDATE
import vn.io.huangnosimp.extension.ConduitExtension
import vn.io.huangnosimp.tasks.CleanConduitCache
import vn.io.huangnosimp.tasks.DownloadMcManifest
import vn.io.huangnosimp.utils.registerMcSetupPipeline
import vn.io.huangnosimp.utils.registerPatchingPipeline

class Conduit : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create("conduit", ConduitExtension::class.java)
        if (!project.plugins.hasPlugin("java")) {
            project.plugins.apply("java")
        }
        val downloadMcManifest =
            project.tasks.register("downloadMcManifest", DownloadMcManifest::class.java) {
                it.mcManifest.set(project.layout.projectDirectory.file(CONDUIT_CACHE_DIR + MC_MANIFEST))
            }
        project.tasks.register("cleanConduitCache", CleanConduitCache::class.java) {
            it.group = "build"
            it.cacheDir.set(project.layout.projectDirectory.dir(CONDUIT_CACHE_DIR))
        }
        project.afterEvaluate {
            if (ext.mcBaseVersion.isPresent) {
                val mcBaseResult =
                    project.registerMcSetupPipeline(
                        type = BASE,
                        version = ext.mcBaseVersion,
                        mcManifest =
                            downloadMcManifest.flatMap {
                                it.mcManifest
                            },
                    )
                val setupMcBaseTask = mcBaseResult.setupMcVersion
                project.registerPatchingPipeline(
                    inputDir =
                        setupMcBaseTask.flatMap { task ->
                            task.sourcesDir
                        },
                    type = SOURCES,
                )
                project.registerPatchingPipeline(
                    inputDir =
                        setupMcBaseTask.flatMap { task ->
                            task.resourcesDir
                        },
                    type = RESOURCES,
                )

                val mcLibs =
                    project.objects
                        .fileCollection()
                        .from(mcBaseResult.libsDir)
                        .asFileTree
                        .matching { it.include("**/*.jar") }

                project.dependencies.add("implementation", mcLibs)
            }
            if (ext.mcUpdateVersion.isPresent) {
                project.registerMcSetupPipeline(
                    type = UPDATE,
                    version = ext.mcUpdateVersion,
                    mcManifest =
                        downloadMcManifest.flatMap {
                            it.mcManifest
                        },
                )
            }
        }
        project.tasks.register("runAll") {
            it.dependsOn("setupSourcesWorkspace")
            it.dependsOn("setupResourcesWorkspace")
        }
    }
}
