package vn.io.huangnosimp.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import vn.io.huangnosimp.utils.Constant
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@CacheableTask
abstract class DownloadMcManifest: DefaultTask() {
    @get:OutputFile
    abstract val outputFile: RegularFileProperty
    @TaskAction
    fun run() {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder(URI(Constant.MC_VERSION_MANIFEST_V2_URL)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            throw GradleException("Could not download minecraft manifest. HTTP Status: ${response.statusCode()}")
        }
        outputFile.get().asFile.writeText(response.body())
    }
}