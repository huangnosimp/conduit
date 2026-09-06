package vn.io.huangnosimp.data.mache

import org.gradle.api.Project

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
}
