package vn.io.huangnosimp.tasks

import com.google.gson.Gson
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import vn.io.huangnosimp.utils.Constant
import vn.io.huangnosimp.utils.McManifest
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.OffsetDateTime

@CacheableTask
abstract class DownloadVersionManifest: DefaultTask() {
    @get:Input
    abstract val mcVersion: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun run() {
        val gson = Gson()
        val json = inputFile.get().asFile.readText(Charsets.UTF_8)
        val mcManifest = gson.fromJson(json, McManifest::class.java)
        val version = mcManifest.versions.associateBy { it.id }[mcVersion.get().trim()]
        if (version != null) {
            if (!OffsetDateTime.parse(version.releaseTime).toInstant().isBefore(Constant.MC_26_RELEASE_TIME)) {
                val client = HttpClient.newHttpClient()
                val request = HttpRequest.newBuilder(URI(version.url)).build()
                val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
                if (response.statusCode() == 200) {
                    val output = outputFile.get().asFile
                    output.writeText(response.body())
                } else {
                    throw RuntimeException("Could not download minecraft version manifest. HTTP status: ${response.statusCode()}.")
                }
            } else {
                throw IllegalArgumentException("plugin only support minecraft version from 26.x.x.")
            }
        } else {
            throw IllegalArgumentException("not found minecraft version ${mcVersion.get()}.")
        }
    }
}