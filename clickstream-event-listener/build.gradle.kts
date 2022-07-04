import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

apply(from = "$rootDir/scripts/versioning.gradle")
apply(from = "$rootDir/scripts/publish-jar-artifact-task.gradle")

ext {
    set("PUBLISH_GROUP_ID", "com.gojek.clickstream")
    set("PUBLISH_ARTIFACT_ID", "clickstream-event-listener")
    set("PUBLISH_VERSION", ext.get("gitVersionName"))
}

if (!project.hasProperty("isLocal")) {
    apply(from = "$rootDir/scripts/publish-module.gradle")
}

tasks.assemble.dependsOn("javadocJar")

java {
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}