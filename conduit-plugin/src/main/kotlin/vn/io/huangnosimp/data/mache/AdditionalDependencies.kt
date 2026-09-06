package vn.io.huangnosimp.data.mache

data class AdditionalDependencies(
    val compileOnly: List<MavenArtifact>? = null,
    val implementation: List<MavenArtifact>? = null,
)
