plugins {
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.plugin.publish)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.gson)
    implementation(libs.jgit)
    implementation(libs.diffpatch)
}

gradlePlugin {
    vcsUrl.set("https://github.com/huangnosimp/conduit.git")
    plugins {
        create("conduit") {
            id = "vn.io.huangnosimp.conduit"
            implementationClass = "vn.io.huangnosimp.Conduit"
            displayName = "Conduit"
            description = "A Gradle plugin designed to generate Minecraft source"
            tags.set(listOf("minecraft", "plugin"))
        }
    }
}

kotlin {
    jvmToolchain(25)
}
