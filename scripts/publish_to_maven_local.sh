#!/usr/bin/env bash

./gradlew :clickstream:assembleRelease -PisLocal -Partifactory && ./gradlew :clickstream:publishToMavenLocal -PisLocal -Partifactory &&
./gradlew :clickstream-api:assembleRelease -PisLocal -Partifactory && ./gradlew :clickstream-api:publishToMavenLocal -PisLocal -Partifactory &&
./gradlew :clickstream-logger:assembleRelease -PisLocal -Partifactory && ./gradlew :clickstream-logger:publishToMavenLocal -PisLocal -Partifactory &&
./gradlew :clickstream-lifecycle:assembleRelease -PisLocal -Partifactory && ./gradlew :clickstream-lifecycle:publishToMavenLocal -PisLocal -Partifactory &&
./gradlew :clickstream-util:assembleRelease -PisLocal -Partifactory && ./gradlew :clickstream-util:publishToMavenLocal -PisLocal -Partifactory &&
./gradlew :clickstream-health-metrics:assembleRelease -PisLocal -Partifactory && ./gradlew :clickstream-health-metrics:publishToMavenLocal -PisLocal -Partifactory &&
./gradlew :clickstream-health-metrics-noop:assembleRelease -PisLocal -Partifactory && ./gradlew :clickstream-health-metrics-noop:publishToMavenLocal -PisLocal -Partifactory &&
./gradlew :clickstream-health-metrics-api:assembleRelease -PisLocal -Partifactory && ./gradlew :clickstream-health-metrics-api:publishToMavenLocal -PisLocal -Partifactory &&
./gradlew :clickstream-event-interceptor:assemble -PisLocal -Partifactory && ./gradlew :clickstream-event-interceptor:publishToMavenLocal -PisLocal -Partifactory &&
./gradlew :clickstream-event-visualiser:assembleRelease -PisLocal -Partifactory && ./gradlew :clickstream-event-visualiser:publishToMavenLocal -PisLocal -Partifactory &&
./gradlew :clickstream-event-visualiser-noop:assembleRelease -PisLocal -Partifactory && ./gradlew :clickstream-event-visualiser-noop:publishToMavenLocal -PisLocal -Partifactory
./gradlew :clickstream-event-visualiser-ui:assembleRelease -PisLocal -Partifactory && ./gradlew :clickstream-event-visualiser-ui:publishToMavenLocal -PisLocal -Partifactory
./gradlew :clickstream-event-visualiser-ui-noop:assembleRelease -PisLocal -Partifactory && ./gradlew :clickstream-event-visualiser-ui-noop:publishToMavenLocal -PisLocal -Partifactory
