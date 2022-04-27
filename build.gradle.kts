import plugin.DetektConfigurationPlugin

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
        classpath("com.android.tools.build:gradle:7.1.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.20")
        classpath("androidx.benchmark:benchmark-gradle-plugin:1.0.0")
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.12")
        classpath("org.jetbrains.kotlinx:binary-compatibility-validator:0.2.4")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.40.5")
        classpath("io.github.gradle-nexus:publish-plugin:1.1.0")
    }
}

subprojects {
    apply<DetektConfigurationPlugin>()
}

val clean by tasks.creating(Delete::class) {
    delete(rootProject.buildDir)
    delete("${rootDir}/buildSrc/build")
    delete("${rootDir}/clickstream/build")
    delete("${rootDir}/report")
}

apply(plugin = "io.github.gradle-nexus.publish-plugin")
apply(from = "${rootDir}/scripts/publish-root.gradle")