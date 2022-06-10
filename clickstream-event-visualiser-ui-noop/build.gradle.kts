import plugin.AndroidLibraryConfigurationPlugin

apply<AndroidLibraryConfigurationPlugin>()
apply(from = "$rootDir/scripts/versioning.gradle")
apply(from = "$rootDir/scripts/publish-artifact-task.gradle")

ext {
    set("PUBLISH_GROUP_ID", "com.gojek.clickstream")
    set("PUBLISH_ARTIFACT_ID", "clickstream-event-visualiser-ui-noop")
    set("PUBLISH_VERSION", ext.get("gitVersionName"))
}

if (!project.hasProperty("isLocal")) {
    apply(from = "$rootDir/scripts/publish-module.gradle")
}

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    kotlin("android.extensions")
    id("androidx.navigation.safeargs")
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

    buildFeatures {
        viewBinding = true
    }
}