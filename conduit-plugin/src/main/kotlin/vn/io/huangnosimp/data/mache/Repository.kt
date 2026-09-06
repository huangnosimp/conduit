package vn.io.huangnosimp.data.mache

data class Repository(
    val url: String,
    val name: String,
    val groups: List<String>? = null,
)
