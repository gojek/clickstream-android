buildscript {
    repositories {
        jcenter()
    }
}

repositories {
    mavenCentral()
    google()
    jcenter()
    maven("https://plugins.gradle.org/m2/")
}

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("com.android.tools.build:gradle:7.0.4")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.0")
    implementation("com.github.node-gradle:gradle-node-plugin:2.2.0")
    implementation("org.codehaus.groovy:groovy:3.0.9")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.15.0")
    // see https://github.com/google/dagger/issues/3068#issuecomment-997883311
    implementation("com.squareup:javapoet:1.13.0")

    implementation(gradleApi())
}
