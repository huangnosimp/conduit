package vn.io.huangnosimp.extension

import org.gradle.api.provider.Property

abstract class ConduitExtension {
    abstract val mcBaseVersion: Property<String>
    abstract val mcUpdateVersion: Property<String>
}
