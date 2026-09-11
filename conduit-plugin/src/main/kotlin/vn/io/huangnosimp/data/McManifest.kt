package vn.io.huangnosimp.data

import com.google.gson.annotations.SerializedName

data class McManifest(
    val versions: List<Version>,
) {
    data class Version(
        val id: String,
        val type: String,
        val url: String,
        val time: String,
        val releaseTime: String,
        val sha1: String,
        val complianceLevel: Int,
    )
}

data class McVersionManifest(
    val downloads: Downloads,
) {
    data class Downloads(
        val server: Server,
        @SerializedName("server_mappings")
        val serverMappings: ServerMappings?,
    ) {
        data class Server(
            val sha1: String,
            val size: Int,
            val url: String,
        )

        data class ServerMappings(
            val sha1: String,
            val size: Int,
            val url: String,
        )
    }
}
