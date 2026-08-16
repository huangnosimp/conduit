package vn.io.huangnosimp.utils.constant

import java.time.Instant
import java.time.OffsetDateTime

const val MC_VERSION_MANIFEST_V2_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
val MC_26_RELEASE_TIME: Instant = OffsetDateTime.parse("2025-12-16T12:42:29+00:00").toInstant()

const val VERSIONS_LIST = "META-INF/versions.list"
const val LIBRARIES_DIR = "META-INF/libraries"