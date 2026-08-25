package vn.io.huangnosimp.constants

import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences
import java.time.Instant
import java.time.OffsetDateTime

const val MC_VERSION_MANIFEST_V2_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
val MC_26_RELEASE_TIME: Instant = OffsetDateTime.parse("2025-12-16T12:42:29+00:00").toInstant()

const val VERSIONS_LIST = "META-INF/versions.list"
const val LIBRARIES_DIR = "META-INF/libraries"

const val CONDUIT_CACHE_DIR = ".gradle/conduit/"
const val MC_MANIFEST = "mcManifest.json"
const val VERSION_MANIFEST = "versionManifest.json"
const val BUNDLER_JAR = "jars/bundler.jar"
const val DECOMPILED_JAR = "jars/decompiled_server.jar"
const val SERVER_JAR = "jars/server.jar"
const val LIBS_DIR = "libs"
const val SOURCES_DIR = "minecraft/java"
const val RESOURCES_DIR = "minecraft/resources"

val DECOMPILE_ARGS: Map<String, String> =
    mapOf(
        "ternary-constant-simplification" to "1",
        "decompile-complex-constant-dynamic" to "1",
        IFernflowerPreferences.INDENT_STRING to "    ",
    )
