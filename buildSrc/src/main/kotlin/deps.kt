@file:Suppress("unused", "ClassName")

import org.gradle.internal.impldep.com.google.api.services.storage.model.Bucket.Lifecycle

object versions {
    internal const val jacoco = "0.8.4"
    internal const val detekt = "1.18.1"

    internal const val kotlin = "1.6.20"
    internal const val coroutines = "1.6.0"

    internal const val scarlet = "0.1.10"
    internal const val okHttp = "3.12.1"

    internal const val room = "2.4.2"
    internal const val lifecycle = "2.2.0"

    internal const val workManagerVersion = "2.7.1"

    internal const val csProtoVersion = "1.18.2"
}

object deps {
    object android {
        object build {
            const val buildToolsVersion = "31.0.0"
            const val compileSdkVersion = 31
            const val minSdkVersion = 19
            const val targetSdkVersion = 31
        }

        object test {
            const val junit = "junit:junit:4.13"
        }
    }

    object kotlin {
        object stdlib {
            const val core = "org.jetbrains.kotlin:kotlin-stdlib:${versions.kotlin}"
            const val jdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${versions.kotlin}"
        }

        object coroutines {
            private const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions.coroutines}"
            private const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${versions.coroutines}"
            private const val reactive = "org.jetbrains.kotlinx:kotlinx-coroutines-reactive:${versions.coroutines}"

            val list = listOf(core, reactive, android)
        }
    }

    object detekt {
        const val lint = "io.gitlab.arturbosch.detekt:detekt-formatting:${versions.detekt}"
        const val cli = "io.gitlab.arturbosch.detekt:detekt-cli:${versions.detekt}"
    }

    object Dagger {
        const val hiltAndroid = "com.google.dagger:hilt-android:2.38.1"
        const val hiltCompiler = "com.google.dagger:hilt-compiler:2.38.1"
        const val hiltAndroidCompiler = "com.google.dagger:hilt-android-compiler:2.38.1"

        const val hiltAndroidTesting = "com.google.dagger:hilt-android-testing:2.38.1"
    }

    object networkLibs {
        const val okHttp = "com.squareup.okhttp3:okhttp:${versions.okHttp}"
        private const val scarlet = "com.tinder.scarlet:scarlet:${versions.scarlet}"
        private const val scarletOkHttpUtils = "com.tinder.scarlet:websocket-okhttp:${versions.scarlet}"
        private const val scarletCoroutineAdapter = "com.tinder.scarlet:stream-adapter-coroutines:${versions.scarlet}"
        private const val scarletLifeCycle = "com.tinder.scarlet:lifecycle-android:${versions.scarlet}"
        const val scarletProtobuf = "com.tinder.scarlet:message-adapter-protobuf:${versions.scarlet}"
        private const val scarletGson = "com.tinder.scarlet:message-adapter-gson:${versions.scarlet}"

        val list = listOf(
            okHttp,
            scarlet,
            scarletOkHttpUtils,
            scarletCoroutineAdapter,
            scarletLifeCycle,
            scarletProtobuf,
            scarletGson
        )
    }

    object room {
        const val room = "androidx.room:room-runtime:${versions.room}"
        const val roomCompiler = "androidx.room:room-compiler:${versions.room}"
        const val roomTesting = "androidx.room:room-testing:${versions.room}"
        const val roomKtx = "androidx.room:room-ktx:${versions.room}"

        val list = listOf(room, roomKtx)
    }

    object lifecycle {
        const val lifeCycleProcess = "androidx.lifecycle:lifecycle-process:${versions.lifecycle}"
    }

    object common {
        val list = listOf(lifecycle.lifeCycleProcess, workManager.workRuntimeKtx)
    }

    object workManager {
        const val workRuntimeKtx = "androidx.work:work-runtime-ktx:${versions.workManagerVersion}"
    }

    object uniTest {
        const val mockito = "org.mockito.kotlin:mockito-kotlin:3.2.0"
        private const val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0"
        private const val assertJCore = "org.assertj:assertj-core:3.9.1"
        private const val mockwebserver = "com.squareup.okhttp3:mockwebserver:4.9.1"
        private const val robolectric = "org.robolectric:robolectric:4.5.1"
        private const val testCore = "androidx.test:core:1.4.0"
        private val workTesting = "androidx.work:work-testing:${versions.workManagerVersion}"
        private const val coroutineTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${versions.coroutines}"

        val list =
            listOf(mockitoKotlin, assertJCore, mockwebserver, robolectric, testCore, workTesting, mockito, coroutineTest)
    }

    object androidTest {
        private const val core = "androidx.test:core:1.4.0"
        private const val coreKtx = "androidx.test:core-ktx:1.4.0"
        private const val junit = "androidx.test.ext:junit:1.1.3"
        private const val junitKtx = "androidx.test.ext:junit-ktx:1.1.3"
        private const val runner = "androidx.test:runner:1.4.0"
        private const val espressoCore = "androidx.test.espresso:espresso-core:3.4.0"
        private const val rules = "androidx.test:rules:1.4.0"
        private const val coreTesting = "androidx.arch.core:core-testing:2.1.0"

        val list = listOf(
            core, coreKtx, junit, junitKtx, rules, espressoCore, rules, coreTesting
        )
    }

    object utils {
        const val protoBufJavaUtil = "com.google.protobuf:protobuf-java-util:3.11.0"
        const val protoLite = "com.google.protobuf:protobuf-javalite:3.11.0"
        const val gson = "com.google.code.gson:gson:2.8.6"

        val list = listOf(protoLite, gson)
    }
}
