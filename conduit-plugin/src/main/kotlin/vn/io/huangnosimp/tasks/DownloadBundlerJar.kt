package vn.io.huangnosimp.tasks

import com.google.gson.Gson
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import vn.io.huangnosimp.data.McVersionManifest
import vn.io.huangnosimp.hashing.toSha1
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

abstract class DownloadBundlerJar : BaseTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val versionManifest: RegularFileProperty

    @get:OutputFile
    abstract val bundlerJar: RegularFileProperty

    @TaskAction
    fun run() {
        val gson = Gson()
        val json = versionManifest.get().asFile.readText(Charsets.UTF_8)
        val versionManifest = gson.fromJson(json, McVersionManifest::class.java)
        val server = versionManifest.downloads.server

        val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()
        val request = HttpRequest.newBuilder(URI(server.url)).timeout(Duration.ofSeconds(60)).build()

        val response =
            try {
                client.send(request, HttpResponse.BodyHandlers.ofByteArray())
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
