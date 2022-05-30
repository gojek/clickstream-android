import plugin.AndroidLibraryConfigurationPlugin

apply<AndroidLibraryConfigurationPlugin>()
apply(from = "$rootDir/scripts/versioning.gradle")

ext {
    set("PUBLISH_GROUP_ID", "com.gojek.android")
    set("PUBLISH_ARTIFACT_ID", "clickstream-health-metrics")
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
    // Clickstream
    implementation(files("$rootDir/libs/proto-sdk-1.18.6.jar"))

    // Common
    deps.common.list.forEach(::implementation)

    // Room
    deps.room.list.forEach(::implementation)
    kapt(deps.room.roomCompiler)

    // Coroutine
    deps.kotlin.coroutines.list.forEach(::implementation)

    // Networking
    deps.networkLibs.list.forEach(::implementation)
    implementation(deps.networkLibs.scarletProtobuf) {
        exclude(group = "com.google.protobuf")
    }

    // Unit Test
    deps.android.test.unitTest.list.forEach(::testImplementation)
    testImplementation(files("libs/proto-consumer-1.18.6.jar"))

    // UI Test
    deps.android.test.uiTest.list.forEach(::androidTestImplementation)
}
