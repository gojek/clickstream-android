package plugin

import com.android.build.gradle.BaseExtension
import deps
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

internal fun Project.configureAndroid() {
    this.extensions.getByType<BaseExtension>().run {
        compileSdkVersion(deps.android.build.targetSdkVersion)
        buildToolsVersion(deps.android.build.buildToolsVersion)
        defaultConfig {
            minSdkVersion(deps.android.build.minSdkVersion)
            targetSdkVersion(deps.android.build.targetSdkVersion)
            consumerProguardFiles("$rootDir/proguard/proguard-rules.pro")
        }

        sourceSets {
            getByName("main").java.srcDir("src/main/kotlin")
            getByName("test").java.srcDir("src/test/kotlin")
            getByName("androidTest").java.srcDir("src/androidTest/kotlin")
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }
}