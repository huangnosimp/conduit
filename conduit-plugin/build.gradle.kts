plugins {
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
}

group = project.group
version = project.version

repositories {
    mavenCentral()
    gradlePluginPortal()
    mavenLocal()
}

dependencies {
}

tasks.test {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("conduit") {
            id = "vn.io.huangnosimp.conduit"
            implementationClass = "vn.io.huangnosimp.conduit"
        }
    }
}

kotlin {
    jvmToolchain(25)
}
