#!/usr/bin/env bash

./gradlew :app:testReleaseUnitTest &&
./gradlew :clickstream:testReleaseUnitTest &&
./gradlew :clickstream-api:testReleaseUnitTest &&
./gradlew :clickstream-logger:testReleaseUnitTest &&
./gradlew :clickstream-lifecycle:testReleaseUnitTest &&
./gradlew :clickstream-util:testReleaseUnitTest &&
./gradlew :clickstream-health-metrics:testReleaseUnitTest &&
./gradlew :clickstream-health-metrics-noop:testReleaseUnitTest &&
./gradlew :clickstream-health-metrics-api:testReleaseUnitTest &&
./gradlew :clickstream-event-listener:assemble &&
./gradlew :clickstream-event-visualiser:assembleRelease &&
./gradlew :clickstream-event-visualiser-noop:assembleRelease &&
./gradlew :clickstream-event-visualiser-ui:assembleRelease &&
./gradlew :clickstream-event-visualiser-ui-noop:assembleRelease