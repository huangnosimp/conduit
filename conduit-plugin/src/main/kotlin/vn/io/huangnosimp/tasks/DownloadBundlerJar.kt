package vn.io.huangnosimp.tasks

import com.google.gson.Gson
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import vn.io.huangnosimp.data.McVersionManifest
import vn.io.huangnosimp.util.toSha1
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@CacheableTask
abstract class DownloadBundlerJar : BaseTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val versionManifest: RegularFileProperty

    @get:OutputFile
    abstract val bundlerJar: RegularFileProperty

    @get:Optional
    @get:OutputFile
    abstract val serverMapping: RegularFileProperty

    @TaskAction
    fun run() {
        serverMapping.get().asFile.delete()
        val gson = Gson()
        val json = versionManifest.get().asFile.readText(Charsets.UTF_8)
        val versionManifest = gson.fromJson(json, McVersionManifest::class.java)
        val server = versionManifest.downloads.server
        val serverMappings = versionManifest.downloads.serverMappings

        val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()
        val serverRequest = HttpRequest.newBuilder(URI(server.url)).timeout(Duration.ofSeconds(60)).build()

        if (serverMappings != null) {
            val serverMappingsRequest = HttpRequest.newBuilder(URI(serverMappings.url)).timeout(Duration.ofSeconds(60)).build()
            val serverMappingsResponse =
                try {
                    client.send(serverMappingsRequest, HttpResponse.BodyHandlers.ofByteArray())
                } catch (e: Exception) {
                    throw GradleException("Failed to download server mappings from ${serverMappings.url}: ${e.message}", e)
                }
            if (serverMappingsResponse.statusCode() != 200) {
                throw GradleException("Can't download server mappings.")
            }
            if (serverMappingsResponse.body().toSha1() != serverMappings.sha1) {
                throw GradleException("File integrity verification failed for server mappings")
            }
            serverMapping.get().asFile.writeBytes(serverMappingsResponse.body())
        }
        val response =
            try {
                client.send(serverRequest, HttpResponse.BodyHandlers.ofByteArray())
            } catch (e: Exception) {
                throw GradleException("Failed to download server from ${server.url}: ${e.message}", e)
            }

        if (response.statusCode() != 200) {
            throw GradleException("Can't download server.jar.")
        }

        if (response.body().toSha1() != server.sha1) {
            throw GradleException("File integrity verification failed")
        }

        bundlerJar.get().asFile.writeBytes(response.body())
    }
}
