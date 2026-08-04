package vn.io.huangnosimp.extension

import org.gradle.api.provider.Property

abstract class ConduitExtension {
    abstract val mcVersion: Property<String>
}