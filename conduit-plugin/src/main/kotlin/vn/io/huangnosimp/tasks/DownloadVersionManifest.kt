package vn.io.huangnosimp.tasks

import com.google.gson.Gson
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import vn.io.huangnosimp.data.McManifest
import vn.io.huangnosimp.util.toSha1
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@CacheableTask
abstract class DownloadVersionManifest : BaseTask() {
    @get:Input
    abstract val mcVersion: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val mcManifest: RegularFileProperty

    @get:OutputFile
    abstract val versionManifest: RegularFileProperty

    @TaskAction
    fun run() {
        val gson = Gson()
        val json = mcManifest.get().asFile.readText(Charsets.UTF_8)
        val mcManifest = gson.fromJson(json, McManifest::class.java)
        val version =
            mcManifest.versions.associateBy { it.id }[mcVersion.get().trim()]
                ?: throw InvalidUserDataException("not found minecraft version ${mcVersion.get()}.")

        val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()
        val request = HttpRequest.newBuilder(URI(version.url)).timeout(Duration.ofSeconds(30)).build()

        val response =
            try {
                client.send(request, HttpResponse.BodyHandlers.ofString())
            } catch (e: Exception) {
                throw GradleException("Failed to download version manifest for ${mcVersion.get()}: ${e.message}", e)
            }

        if (response.statusCode() != 200) {
            throw GradleException("Could not download minecraft version manifest. HTTP status: ${response.statusCode()}.")
        }

        if (response.body().toSha1() != version.sha1) {
            throw GradleException("File integrity verification failed")
        }

        versionManifest.get().asFile.writeText(response.body())
    }
}
