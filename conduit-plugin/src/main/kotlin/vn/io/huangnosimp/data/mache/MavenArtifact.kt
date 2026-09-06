package vn.io.huangnosimp.data.mache

data class MavenArtifact(
    val group: String,
    val name: String,
    val version: String,
    val classifier: String? = null,
    val extension: String? = null,
) {
    fun toMavenString(): String = "$group:$name:$version" + (classifier?.let { ":$it" } ?: "") + (extension?.let { "@$it" } ?: "")
}
