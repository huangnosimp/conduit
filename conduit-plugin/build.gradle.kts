plugins {
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.gson)
    implementation(variantOf(libs.vineflower) { classifier("slim") })
    implementation(libs.jgit)
    implementation(libs.diffpatch)
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
