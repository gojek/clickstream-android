#!/usr/bin/env bash

./gradlew :clickstream:assembleRelease && ./gradlew :clickstream:publishToMavenLocal &&
./gradlew :clickstream-api:assembleRelease && ./gradlew :clickstream-api:publishToMavenLocal &&
./gradlew :clickstream-logger:assembleRelease && ./gradlew :clickstream-logger:publishToMavenLocal &&
./gradlew :clickstream-lifecycle:assembleRelease && ./gradlew :clickstream-lifecycle:publishToMavenLocal &&
./gradlew :clickstream-util:assembleRelease && ./gradlew :clickstream-util:publishToMavenLocal &&
./gradlew :clickstream-health-metrics:assembleRelease && ./gradlew :clickstream-health-metrics:publishToMavenLocal &&
./gradlew :clickstream-health-metrics-api:assembleRelease && ./gradlew :clickstream-health-metrics-api:publishToMavenLocal