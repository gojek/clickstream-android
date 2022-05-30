#!/usr/bin/env bash

./gradlew :clickstream:assembleRelease && ./gradlew :clickstream:publishToMavenLocal
./gradlew :clickstream-health-metrics:assembleRelease && ./gradlew :clickstream-health-metrics:publishToMavenLocal
./gradlew :clickstream-health-metrics-api:assembleRelease && ./gradlew :clickstream-health-metrics-api:publishToMavenLocal