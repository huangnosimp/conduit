plugins {
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.gson)
    implementation(libs.vineflower)
    implementation(libs.jgit)
}

gradlePlugin {
    plugins {
        create("conduit") {
            id = "vn.io.huangnosimp.conduit"
            implementationClass = "vn.io.huangnosimp.Conduit"
        }
    }
}

kotlin {
    jvmToolchain(25)
}
