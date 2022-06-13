import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

val propFile: File = project.rootProject.file("local.properties")
val props = Properties()
if (propFile.exists()) {
    val fileInput = FileInputStream(propFile)
    props.load(fileInput)
    props.forEach { (key, value) ->
        println("Key : $key Value : $value")
    }
}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "com.clickstream.app"
        minSdk = 21
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "ACCOUNT_ID", "\"${props.getProperty("accountId")}\"")
        buildConfigField("String", "SECRET_KEY", "\"${props.getProperty("secretKey")}\"")
        buildConfigField("String", "ENDPOINT", "\"${props.getProperty("endpoint")}\"")
        buildConfigField("String", "STUB_BEARER", "\"${props.getProperty("bearer")}\"")
    }

    sourceSets {
        getByName("main").java.srcDir("src/main/kotlin")
        getByName("test").java.srcDir("src/test/kotlin")
        getByName("androidTest").java.srcDir("src/androidTest/kotlin")
    }

    buildTypes.getByName("debug") {
        isTestCoverageEnabled = true
        isDebuggable = true
    }

    buildTypes.getByName("release") {
        isTestCoverageEnabled = false
        isDebuggable = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xinline-classes")
    }

    buildFeatures {
        viewBinding = true
    }
}

kapt {
    // For Hilt Setup
    // https://dagger.dev/hilt/gradle-setup
    correctErrorTypes = true
}

hilt {
    // The Hilt configuration option 'enableTransformForLocalTests'
    // is no longer necessary when com.android.tools.build:gradle:4.2.0+ is used.
    // enableTransformForLocalTests = true
    enableAggregatingTask = true

    // see
    // https://github.com/google/dagger/issues/1991
    // https://github.com/google/dagger/issues/970
    enableExperimentalClasspathAggregation = true
}

// clickstream uses protobuf-life as an internal artifact.
// However few google libraries uses protobuf-java transitively, hence we have to substitute
// the protobuf-java to use protobuf-javalite
configurations.all {
    resolutionStrategy {
        dependencySubstitution {
            substitute(module("com.google.protobuf:protobuf-java:3.11.0"))
                .because("protobuf javalite supercedes protobuf-java")
                .with(module("com.google.protobuf:protobuf-javalite:3.11.0"))

            substitute(module("com.google.protobuf:protobuf-java:2.6.1"))
                .because("protobuf javalite supercedes protobuf-java")
                .with(module("com.google.protobuf:protobuf-javalite:3.11.0"))
        }
    }
}

dependencies {
    // Hilt
    implementation(deps.Dagger.hiltAndroid)
    kapt(deps.Dagger.hiltCompiler)
    kapt(deps.Dagger.hiltAndroidCompiler)
    implementation(deps.utils.gson)
    implementation(deps.networkLibs.okHttp)

    // Coroutine
    deps.kotlin.coroutines.list.forEach(::implementation)
    implementation(deps.kotlin.stdlib.core)
    implementation(deps.kotlin.stdlib.jdk8)

    // Clickstream
    implementation(projects.clickstream)
    implementation(projects.clickstreamEventVisualiser)
    implementation(projects.clickstreamEventInterceptor)
    implementation(files("$rootDir/libs/proto-consumer-1.18.6.jar"))

    // Common
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
    implementation("androidx.core:core-ktx:1.5.0")
    implementation("androidx.fragment:fragment-ktx:1.4.1")
    implementation("androidx.appcompat:appcompat:1.1.0-rc01")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.1.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.1.0")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-android:1.2.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}