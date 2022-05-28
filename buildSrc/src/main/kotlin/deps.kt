@file:Suppress("unused", "ClassName")

object versions {
    internal const val jacoco = "0.8.4"
    internal const val detekt = "1.1.1"

    internal const val kotlin = "1.4.32"
    internal const val coroutines = "1.4.3"

    internal const val scarlet = "0.1.10"
    internal const val okHttp = "3.12.1"

    internal const val room = "2.2.3"
    internal const val lifecycle = "2.2.0"

    internal const val workManagerVersion = "2.3.4"

    internal const val csProtoVersion = "1.18.2"
}

object deps {
    object android {
        object build {
            const val buildToolsVersion = "29.0.2"
            const val compileSdkVersion = 29
            const val minSdkVersion = 19
            const val targetSdkVersion = 29
        }

        object test {
            const val junit = "junit:junit:4.13"
            const val mockito = "org.mockito.kotlin:mockito-kotlin:3.2.0"

            object unitTest {
                val list = listOf(
                    deps.android.test.mockito,
                    "com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0",
                    deps.kotlin.coroutines.test,
                    "org.assertj:assertj-core:3.9.1",
                    "com.squareup.okhttp3:mockwebserver:4.9.1",
                    "org.robolectric:robolectric:4.5.1",
                    "androidx.test:core:1.4.0"
                )
            }

            object uiTest {
                val list = listOf(
                    deps.workManager.workTesting,
                    "androidx.test:core:1.4.0",
                    "androidx.test:core-ktx:1.4.0",
                    "androidx.test.ext:junit:1.1.3",
                    "androidx.test.ext:junit-ktx:1.1.3",
                    "androidx.test:runner:1.4.0",
                    "androidx.test.espresso:espresso-core:3.4.0",
                    "androidx.test:rules:1.4.0",
                    "androidx.arch.core:core-testing:2.1.0"
                )
            }
        }
    }

    object Dagger {
        const val hiltAndroid = "com.google.dagger:hilt-android:2.38.1"
        const val hiltCompiler = "com.google.dagger:hilt-compiler:2.38.1"
        const val hiltAndroidCompiler = "com.google.dagger:hilt-android-compiler:2.38.1"

        const val hiltAndroidTesting = "com.google.dagger:hilt-android-testing:2.38.1"
    }

    object kotlin {
        object stdlib {
            const val core = "org.jetbrains.kotlin:kotlin-stdlib:${versions.kotlin}"
            const val jdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${versions.kotlin}"
        }

        object coroutines {
            const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions.coroutines}"
            const val android =
                "org.jetbrains.kotlinx:kotlinx-coroutines-android:${versions.coroutines}"
            const val reactive =
                "org.jetbrains.kotlinx:kotlinx-coroutines-reactive:${versions.coroutines}"
            const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${versions.coroutines}"

            val list = listOf(
                core, reactive
            )
        }
    }

    object detekt {
        const val lint = "io.gitlab.arturbosch.detekt:detekt-formatting:${versions.detekt}"
        const val cli = "io.gitlab.arturbosch.detekt:detekt-cli:${versions.detekt}"
    }

    object networkLibs {
        const val okHttp = "com.squareup.okhttp3:okhttp:${versions.okHttp}"
        const val scarlet = "com.tinder.scarlet:scarlet:${versions.scarlet}"
        const val scarletOkHttpUtils = "com.tinder.scarlet:websocket-okhttp:${versions.scarlet}"
        const val scarletCoroutineAdapter =
            "com.tinder.scarlet:stream-adapter-coroutines:${versions.scarlet}"
        const val scarletLifeCycle = "com.tinder.scarlet:lifecycle-android:${versions.scarlet}"
        const val scarletProtobuf =
            "com.tinder.scarlet:message-adapter-protobuf:${versions.scarlet}"
        const val scarletGson = "com.tinder.scarlet:message-adapter-gson:${versions.scarlet}"

        val list = listOf(
            scarlet, scarletLifeCycle, scarletOkHttpUtils,
            scarletCoroutineAdapter,
            okHttp
        )
    }

    object room {
        const val room = "androidx.room:room-runtime:${versions.room}"
        const val roomCompiler = "androidx.room:room-compiler:${versions.room}"
        const val roomTesting = "androidx.room:room-testing:${versions.room}"
        const val roomKtx = "androidx.room:room-ktx:${versions.room}"

        val list = listOf(
            room, roomKtx
        )
    }

    object lifecycle {
        const val lifeCycleProcess = "androidx.lifecycle:lifecycle-process:${versions.lifecycle}"
    }

    object workManager {
        const val workRuntimeKtx = "androidx.work:work-runtime-ktx:${versions.workManagerVersion}"
        const val workTesting = "androidx.work:work-testing:${versions.workManagerVersion}"

    }

    object utils {
        const val protoBufJavaUtil = "com.google.protobuf:protobuf-java-util:3.11.0"
        const val protoLite = "com.google.protobuf:protobuf-javalite:3.11.0"
        const val gson = "com.google.code.gson:gson:2.8.6"
    }

    object common {
        val list = listOf(
            deps.utils.protoLite,
            deps.utils.gson,
            deps.workManager.workRuntimeKtx,
            deps.lifecycle.lifeCycleProcess
        )
    }
}
