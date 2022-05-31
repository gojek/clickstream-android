#!/usr/bin/env bash

./gradlew :clickstream:assembleRelease -Partifactory && ./gradlew :clickstream:artifactoryPublish -Partifactory &&
./gradlew :clickstream-api:assembleRelease -Partifactory && ./gradlew :clickstream-api:artifactoryPublish -Partifactory &&
./gradlew :clickstream-logger:assembleRelease -Partifactory && ./gradlew :clickstream-logger:artifactoryPublish -Partifactory &&
./gradlew :clickstream-lifecycle:assembleRelease -Partifactory && ./gradlew :clickstream-lifecycle:artifactoryPublish -Partifactory &&
./gradlew :clickstream-util:assembleRelease -Partifactory && ./gradlew :clickstream-util:artifactoryPublish -Partifactory &&
./gradlew :clickstream-health-metrics:assembleRelease -Partifactory && ./gradlew :clickstream-health-metrics:artifactoryPublish -Partifactory &&
./gradlew :clickstream-health-metrics-noop:assembleRelease -Partifactory && ./gradlew :clickstream-health-metrics-noop:artifactoryPublish -Partifactory &&
./gradlew :clickstream-health-metrics-api:assembleRelease -Partifactory && ./gradlew :clickstream-health-metrics-api:artifactoryPublish -Partifactory