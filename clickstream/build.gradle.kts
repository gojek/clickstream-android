    import plugin.AndroidLibraryConfigurationPlugin

apply<AndroidLibraryConfigurationPlugin>()
apply(from = "$rootDir/scripts/versioning.gradle")
apply(from = "$rootDir/scripts/publish-artifact-task.gradle")

ext {
    set("PUBLISH_GROUP_ID", "com.gojek.clickstream")
    set("PUBLISH_ARTIFACT_ID", "clickstream-android")
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
}

android {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-XXLanguage:+InlineClasses", "-Xexplicit-api=strict")
    }
    defaultConfig {
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("$rootDir/proguard/consumer-proguard-rules.pro")
    }
}

dependencies {
    // Clickstream
    implementation(files("$rootDir/libs/proto-sdk-1.18.6.jar"))
    api(projects.clickstreamLogger)
    api(projects.clickstreamHealthMetricsNoop)
    api(projects.clickstreamEventListener)
    compileOnly(projects.clickstreamApi)
    compileOnly(projects.clickstreamHealthMetricsApi)
    compileOnly(projects.clickstreamLifecycle)

    // Proto
    api(deps.utils.protoLite)

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
    testImplementation(files("$rootDir/libs/proto-consumer-1.18.6.jar"))
    testImplementation(projects.clickstreamHealthMetrics)
    testImplementation(projects.clickstreamApi)
    testImplementation(projects.clickstreamHealthMetricsApi)

    // UI Test
    deps.android.test.uiTest.list.forEach(::androidTestImplementation)
}
