import plugin.AndroidLibraryConfigurationPlugin

apply<AndroidLibraryConfigurationPlugin>()
apply(from = "$rootDir/scripts/versioning.gradle")
apply(from = "$rootDir/scripts/publish-artifact-task.gradle")

ext {
    set("PUBLISH_GROUP_ID", "com.gojek.clickstream")
    set("PUBLISH_ARTIFACT_ID", "clickstream-event-visualiser-ui")
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

dependencies {

    //kotlin core
    implementation(deps.kotlin.coroutines.core)
    implementation(deps.kotlin.stdlib.core)
    implementation(deps.kotlin.stdlib.jdk8)

    //android core
    implementation(deps.android.core.ktx)
    implementation(deps.android.core.appCompat)
    implementation(deps.android.core.material)
    implementation(deps.android.core.constraintLayout)

    //jetpack navigation
    implementation(deps.navigation.fragment)
    implementation(deps.navigation.ui)

    //clickstream
    implementation(projects.clickstreamEventListener)
    implementation(projects.clickstreamEventVisualiser)

    //viewmodel and lifecycle
    implementation(deps.lifecycle.runtime)
    implementation(deps.lifecycle.viewmodel)


    deps.android.test.unitTest.list.forEach(::testImplementation)
    testImplementation(projects.clickstreamEventVisualiser)
    testImplementation(projects.clickstreamEventListener)
}