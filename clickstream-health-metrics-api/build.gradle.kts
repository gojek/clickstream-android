import plugin.AndroidLibraryConfigurationPlugin

apply<AndroidLibraryConfigurationPlugin>()

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    kotlin("android.extensions")
}

android {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xexplicit-api=strict")
    }
    defaultConfig {
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    api(project(":clickstream-health-metrics-model"))
}

ext {
    set("PUBLISH_GROUP_ID", "com.gojek.clickstream")
    set("PUBLISH_VERSION", "0.0.3")
    set("PUBLISH_ARTIFACT_ID", "clickstream-health-metrics-api")
}

apply(from = "$rootDir/scripts/publish-module.gradle")