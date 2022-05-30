import plugin.AndroidLibraryConfigurationPlugin

apply<AndroidLibraryConfigurationPlugin>()
apply(from = "$rootDir/scripts/versioning.gradle")

ext {
    set("PUBLISH_GROUP_ID", "com.gojek.android")
    set("PUBLISH_ARTIFACT_ID", "clickstream-lifecyle")
    set("PUBLISH_VERSION", ext.get("gitVersionName"))
}

apply(from = "$rootDir/scripts/publish-module.gradle")

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    kotlin("android.extensions")
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

dependencies {
    // Common
    deps.common.list.forEach(::implementation)

    // Networking
    implementation(deps.networkLibs.scarlet)
}
