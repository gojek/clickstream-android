import plugin.AndroidLibraryConfigurationPlugin

apply<AndroidLibraryConfigurationPlugin>()
apply(from = "$rootDir/scripts/versioning.gradle")
apply(from = "$rootDir/scripts/publish-artifact-task.gradle")

ext {
    set("PUBLISH_GROUP_ID", "com.gojek.clickstream")
    set("PUBLISH_ARTIFACT_ID", "clickstream-event-visualiser")
    set("PUBLISH_VERSION", ext.get("gitVersionName"))
}

if (!project.hasProperty("isLocal")) {
    apply(from = "$rootDir/scripts/publish-module.gradle")
}

android {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-XXLanguage:+InlineClasses", "-Xexplicit-api=strict")
    }
    defaultConfig {
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    kotlin("android.extensions")

}

dependencies {
    compileOnly(files("$rootDir/libs/proto-sdk-1.18.6.jar"))
    compileOnly(projects.clickstream)
}