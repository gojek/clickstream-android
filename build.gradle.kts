import plugin.DetektConfigurationPlugin

plugins {
    id("org.jetbrains.dokka") version "1.4.32"
}

apply(plugin = "binary-compatibility-validator")

buildscript {
    repositories {
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://maven-central-asia.storage-download.googleapis.com/repos/central/data")
        mavenCentral()
        google()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32")
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.14")
        classpath("org.jetbrains.kotlinx:binary-compatibility-validator:0.8.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.41")
        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.15.0")
        classpath("io.github.gradle-nexus:publish-plugin:1.1.0")
    }
}

allprojects {
    repositories {
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://maven-central-asia.storage-download.googleapis.com/repos/central/data")
        mavenCentral()
        google()
        jcenter()
    }
}

tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(rootDir.resolve("docs/api"))
}

subprojects {
    apply<DetektConfigurationPlugin>()
    apply(plugin = "org.jetbrains.dokka")
}

val clean by tasks.creating(Delete::class) {
    delete(rootProject.buildDir)
    delete("${rootDir}/buildSrc/build")
    delete("${rootDir}/clickstream/build")
    delete("${rootDir}/report")
}

apply(plugin = "io.github.gradle-nexus.publish-plugin")
apply(from = "${rootDir}/scripts/publish-root.gradle")