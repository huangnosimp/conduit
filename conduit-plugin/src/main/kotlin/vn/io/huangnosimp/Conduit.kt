package vn.io.huangnosimp

import com.google.gson.Gson
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import vn.io.huangnosimp.attribute.MacheOutput
import vn.io.huangnosimp.constants.DOWNLOAD_MC_MANIFEST
import vn.io.huangnosimp.constants.MACHE_CODEBOOK_CONFIG
import vn.io.huangnosimp.constants.MACHE_CONFIG
import vn.io.huangnosimp.constants.MACHE_CONSTANTS_CONFIG
import vn.io.huangnosimp.constants.MACHE_DECOMPILER_CONFIG
import vn.io.huangnosimp.constants.MACHE_PARAM_MAPPINGS_CONFIG
import vn.io.huangnosimp.constants.MACHE_REMAPPER_CONFIG
import vn.io.huangnosimp.constants.MC_MANIFEST
import vn.io.huangnosimp.constants.MINECRAFT_DEPENDENCIES_CONFIG
import vn.io.huangnosimp.constants.PAPERMC_REPOSITORY_URL
import vn.io.huangnosimp.data.mache.MacheMetaData
import vn.io.huangnosimp.extension.ConduitExtension
import vn.io.huangnosimp.taskcontainers.SetupTasks
import vn.io.huangnosimp.tasks.DownloadMcManifest

class Conduit : Plugin<Project> {
    override fun apply(project: Project) {
        // Extension
        val ext = project.extensions.create("conduit", ConduitExtension::class.java)
        if (!project.plugins.hasPlugin("java")) {
            project.plugins.apply("java")
        }

        // project config
        val sourceSets =
            project.extensions
                .getByType(JavaPluginExtension::class.java)
                .sourceSets
                .getByName(SourceSet.MAIN_SOURCE_SET_NAME)

        project.repositories.apply {
            maven { it.url = project.uri(PAPERMC_REPOSITORY_URL) }
        }

        project.configurations.register(MACHE_CODEBOOK_CONFIG) {
            it.isTransitive = false
        }
        project.configurations.register(MACHE_PARAM_MAPPINGS_CONFIG) {
            it.isTransitive = false
        }
        project.configurations.register(MACHE_CONSTANTS_CONFIG) {
            it.isTransitive = false
        }
        project.configurations.register(MACHE_REMAPPER_CONFIG) {
            it.isTransitive = false
        }
        project.configurations.register(MACHE_DECOMPILER_CONFIG) {
            it.isTransitive = false
        }
        val macheConfig =
            project.configurations
                .create(MACHE_CONFIG) {
                    it.attributes.attribute(
                        MacheOutput.ATTRIBUTE,
                        project.objects.named(
                            MacheOutput::class.java,
                            MacheOutput.ZIP,
                        ),
                    )
                }.defaultDependencies {
                    val mcVersion = ext.mcVersion.get()
                    it.add(project.dependencies.create("io.papermc:mache:$mcVersion+build.+"))
                }

        val minecraftDependenciesConfig =
            project.configurations.register(MINECRAFT_DEPENDENCIES_CONFIG) {
                it.extendsFrom(macheConfig)
            }

        project.configurations.named(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME) {
            it.extendsFrom(minecraftDependenciesConfig.get())
        }

        val mache: Property<MacheMetaData> = project.objects.property(MacheMetaData::class.java)

        project.afterEvaluate {
            mache.set(
                Gson().fromJson(
                    project
                        .zipTree(macheConfig.singleFile)
                        .files
                        .first { it.name == "mache.json" }
                        .readText(),
                    MacheMetaData::class.java,
                ),
            )

            mache.get().addRepository(project)
            mache.get().addDependencies(project)

            sourceSets.java.srcDirs(project.layout.projectDirectory.dir("src/minecraft/java"))
            sourceSets.resources.srcDirs(project.layout.projectDirectory.dir("src/minecraft/resources"))
        }

        // Task register
        val downloadMcManifest =
            project.tasks.register(DOWNLOAD_MC_MANIFEST, DownloadMcManifest::class.java) {
                it.mcManifest.set(project.layout.projectDirectory.file(MC_MANIFEST))
            }
        val setupTasks = SetupTasks(project, downloadMcManifest, ext.mcVersion, macheConfig, mache)
    }
}
