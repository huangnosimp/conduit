package vn.io.huangnosimp.data.mache

data class Dependencies(
    val codebook: List<MavenArtifact>,
    val paramMappings: List<MavenArtifact>?,
    val constants: List<MavenArtifact>,
    val remapper: List<MavenArtifact>?,
    val decompiler: List<MavenArtifact>,
)
