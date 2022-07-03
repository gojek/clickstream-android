#!/usr/bin/env bash

./gradlew :app:assembleRelease &&
./gradlew :clickstream:assembleRelease &&
./gradlew :clickstream-api:assembleRelease &&
./gradlew :clickstream-logger:assembleRelease &&
./gradlew :clickstream-lifecycle:assembleRelease &&
./gradlew :clickstream-util:assembleRelease &&
./gradlew :clickstream-health-metrics:assembleRelease &&
./gradlew :clickstream-health-metrics-noop:assembleRelease &&
./gradlew :clickstream-health-metrics-api:assembleRelease &&
./gradlew :clickstream-event-listener:assemble &&
./gradlew :clickstream-event-visualiser:assembleRelease &&
./gradlew :clickstream-event-visualiser-noop:assembleRelease &&
./gradlew :clickstream-event-visualiser-ui:assembleRelease &&
./gradlew :clickstream-event-visualiser-ui-noop:assembleRelease