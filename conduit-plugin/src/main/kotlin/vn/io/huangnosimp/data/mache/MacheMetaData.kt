package vn.io.huangnosimp.data.mache

import org.gradle.api.Project
import vn.io.huangnosimp.constants.MACHE_CODEBOOK_CONFIG
import vn.io.huangnosimp.constants.MACHE_CONSTANTS_CONFIG
import vn.io.huangnosimp.constants.MACHE_DECOMPILER_CONFIG
import vn.io.huangnosimp.constants.MACHE_PARAM_MAPPINGS_CONFIG
import vn.io.huangnosimp.constants.MACHE_REMAPPER_CONFIG

data class MacheMetaData(
    val minecraftVersion: String,
    val macheVersion: String,
    val dependencies: Dependencies,
    val repositories: List<Repository>,
    val decompilerArgs: List<String>,
    val remapperArgs: List<String>,
    val additionalCompileDependencies: AdditionalDependencies? = null,
) {
    fun addRepository(project: Project) {
        repositories.forEach { repository ->
            project.repositories.maven { maven ->
                maven.name = repository.name
                maven.setUrl(repository.url)
                if (!repository.groups.isNullOrEmpty()) {
                    maven.content { content ->
                        repository.groups.forEach { group ->
                            content.includeGroup(group)
                        }
                    }
                }
            }
        }
    }

    fun addDependencies(project: Project) {
        val macheDeps = this@MacheMetaData.dependencies
        val configurations = project.configurations
        configurations.named(MACHE_CODEBOOK_CONFIG).configure { config ->
            config.defaultDependencies { deps ->
                macheDeps.codebook.forEach { deps.add(project.dependencies.create(it.toMavenString())) }
            }
        }
        configurations.named(MACHE_PARAM_MAPPINGS_CONFIG).configure { config ->
            config.defaultDependencies { deps ->
                macheDeps.paramMappings?.forEach { deps.add(project.dependencies.create(it.toMavenString())) }
            }
        }
        configurations.named(MACHE_CONSTANTS_CONFIG).configure { config ->
            config.defaultDependencies { deps ->
                macheDeps.constants.forEach { deps.add(project.dependencies.create(it.toMavenString())) }
            }
        }
        configurations.named(MACHE_REMAPPER_CONFIG).configure { config ->
            config.defaultDependencies { deps ->
                macheDeps.remapper?.forEach { deps.add(project.dependencies.create(it.toMavenString())) }
            }
        }
        configurations.named(MACHE_DECOMPILER_CONFIG).configure { config ->
            config.defaultDependencies { deps ->
                macheDeps.decompiler.forEach { deps.add(project.dependencies.create(it.toMavenString())) }
            }
        }
    }
}
