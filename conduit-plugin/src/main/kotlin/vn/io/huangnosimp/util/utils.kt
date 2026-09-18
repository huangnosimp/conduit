package vn.io.huangnosimp.util

import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.use

fun copy(
    sourceRoot: Path,
    targetRoot: Path,
) {
    targetRoot.toFile().deleteRecursively()
    Files.walk(sourceRoot).use { stream ->
        stream.forEach { source ->
            val relative = sourceRoot.relativize(source).toString()
            val target = targetRoot.resolve(relative)

            if (Files.isDirectory(source)) {
                Files.createDirectories(target)
            } else {
                target.parent?.let { Files.createDirectories(it) }
                Files.copy(source, target)
            }
        }
    }
}

fun String.toSha1(): String = toByteArray(Charsets.UTF_8).toSha1()

fun ByteArray.toSha1(): String =
    MessageDigest
        .getInstance("SHA-1")
        .digest(this)
        .joinToString("") { "%02x".format(it.toInt() and 0xff) }
