package vn.io.huangnosimp.tasks

import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import vn.io.huangnosimp.constants.MC_VERSION_MANIFEST_V2_URL
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

abstract class DownloadMcManifest : BaseTask() {
    @get:OutputFile
    abstract val mcManifest: RegularFileProperty

    @TaskAction
    fun run() {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder(URI(MC_VERSION_MANIFEST_V2_URL)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            throw GradleException("Could not download minecraft manifest. HTTP Status: ${response.statusCode()}")
        }
        mcManifest.get().asFile.writeText(response.body())
    }
}
