package vn.io.huangnosimp.worker

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.workers.WorkParameters

interface DecompileServerJarParameters : WorkParameters {
    val serverJar: RegularFileProperty
    val libsDir: DirectoryProperty
    val outputJar: RegularFileProperty
}
