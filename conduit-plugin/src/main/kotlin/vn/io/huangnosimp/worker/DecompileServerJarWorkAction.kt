package vn.io.huangnosimp.worker

import org.gradle.workers.WorkAction
import org.jetbrains.java.decompiler.api.Decompiler
import org.jetbrains.java.decompiler.main.decompiler.SingleFileSaver
import vn.io.huangnosimp.constants.DECOMPILE_ARGS

abstract class DecompileServerJarWorkAction : WorkAction<DecompileServerJarParameters> {
    override fun execute() {
        val decompiler =
            Decompiler
                .builder()
                .inputs(parameters.serverJar.get().asFile)
                .output(SingleFileSaver(parameters.outputJar.get().asFile))
        DECOMPILE_ARGS.forEach { (key, value) -> decompiler.option(key, value) }
        parameters.libsDir.asFileTree
            .matching {
                it.include("**/*.jar")
            }.files
            .forEach { jarFile ->
                decompiler.libraries(jarFile)
            }
        decompiler.build().decompile()
    }
}
