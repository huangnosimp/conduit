package vn.io.huangnosimp.utils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

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
