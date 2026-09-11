package vn.io.huangnosimp.util

import org.gradle.api.GradleException
import vn.io.huangnosimp.constants.MACHE_METADATA_URL
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.use

fun copy(
    sourceRoot: Path,
    targetRoot: Path,
) {
    Files.walk(sourceRoot).use { stream ->
        stream.forEach { source ->
            val relative = sourceRoot.relativize(source).toString()
            val target = targetRoot.resolve(relative)

            if (Files.isDirectory(source)) {
                Files.createDirectories(target)
            } else {
                target.parent?.let { Files.createDirectories(it) }
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
}

fun resolveLatestMacheVersion(minecraftVersion: String): String {
    val xml = URI(MACHE_METADATA_URL).toURL().openStream().use { it.readBytes() }

    val doc =
        DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(xml.inputStream())

    val versionNodes = doc.getElementsByTagName("version")
    val allVersions = (0 until versionNodes.length).map { versionNodes.item(it).textContent }

    val matching = allVersions.filter { it.startsWith("$minecraftVersion+") }
    if (matching.isEmpty()) {
        throw GradleException(
            "No Mache build found for Minecraft $minecraftVersion.\n" +
                    "Available versions: ${allVersions.joinToString(", ")}",
        )
    }

    fun buildNumber(v: String) =
        Regex("""build\.?(\d+)""")
            .find(v)
            ?.groupValues
            ?.get(1)
            ?.toIntOrNull() ?: 0

    return matching.maxBy(::buildNumber)
}