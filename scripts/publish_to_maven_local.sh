#!/usr/bin/env bash

./gradlew :clickstream:assembleRelease -PisLocal && ./gradlew :clickstream:publishToMavenLocal -PisLocal &&
./gradlew :clickstream-api:assembleRelease -PisLocal && ./gradlew :clickstream-api:publishToMavenLocal -PisLocal &&
./gradlew :clickstream-logger:assembleRelease -PisLocal && ./gradlew :clickstream-logger:publishToMavenLocal -PisLocal &&
./gradlew :clickstream-lifecycle:assembleRelease -PisLocal && ./gradlew :clickstream-lifecycle:publishToMavenLocal -PisLocal &&
./gradlew :clickstream-util:assembleRelease -PisLocal && ./gradlew :clickstream-util:publishToMavenLocal -PisLocal &&
./gradlew :clickstream-health-metrics:assembleRelease -PisLocal && ./gradlew :clickstream-health-metrics:publishToMavenLocal -PisLocal &&
./gradlew :clickstream-health-metrics-noop:assembleRelease -PisLocal && ./gradlew :clickstream-health-metrics-noop:publishToMavenLocal -PisLocal &&
./gradlew :clickstream-health-metrics-api:assembleRelease -PisLocal && ./gradlew :clickstream-health-metrics-api:publishToMavenLocal -PisLocal