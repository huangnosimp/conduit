package vn.io.huangnosimp.tasks

import org.gradle.api.DefaultTask

abstract class BaseTask : DefaultTask() {
    init {
        group = "conduit"
    }
}
