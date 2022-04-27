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
    defaultConfig{
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(files("libs/proto-sdk-1.18.6.jar"))

    // Utils
    deps.utils.list.forEach(::implementation)

    // Android Room
    deps.room.list.forEach(::implementation)
    kapt(deps.room.roomCompiler)

    // Coroutine
    deps.kotlin.coroutines.list.forEach(::implementation)

    // Networking
    deps.networkLibs.list.forEach(::implementation)
    implementation(deps.networkLibs.scarletProtobuf) {
        exclude(group = "com.google.protobuf")
    }

    // Common
    deps.common.list.forEach(::implementation)

    // Unit Test
    testImplementation(files("libs/proto-consumer-1.18.6.jar"))
    deps.uniTest.list.forEach(::testImplementation)

    // Android Test
    deps.androidTest.list.forEach(::androidTestImplementation)
}

ext {
    set("PUBLISH_GROUP_ID", "com.gojek.clickstream")
    set("PUBLISH_VERSION", "0.0.1")
    set("PUBLISH_ARTIFACT_ID", "clickstream-android")
}

apply(from = "$rootDir/scripts/publish-module.gradle")