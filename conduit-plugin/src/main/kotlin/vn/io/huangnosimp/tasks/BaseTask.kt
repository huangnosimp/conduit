package vn.io.huangnosimp.tasks

import org.gradle.api.DefaultTask
import vn.io.huangnosimp.constants.CONDUIT

abstract class BaseTask : DefaultTask() {
    init {
        group = CONDUIT
    }
}
