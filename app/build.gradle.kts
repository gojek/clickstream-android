plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
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
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
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

dependencies {
    // Hilt
    implementation(deps.Dagger.hiltAndroid)
    kapt(deps.Dagger.hiltCompiler)
    kapt(deps.Dagger.hiltAndroidCompiler)

    implementation(deps.networkLibs.okHttp)

    implementation("io.github.reactivecircus.flowbinding:flowbinding-android:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.0-alpha06")

    implementation(project(":clickstream"))

    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("androidx.navigation:navigation-fragment-ktx:2.4.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.4.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}