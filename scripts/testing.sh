#!/usr/bin/env bash

./gradlew :clickstream:testReleaseUnitTest &&
./gradlew :clickstream-api:testReleaseUnitTest &&
./gradlew :clickstream-logger:testReleaseUnitTest &&
./gradlew :clickstream-lifecycle:testReleaseUnitTest &&
./gradlew :clickstream-util:testReleaseUnitTest &&
./gradlew :clickstream-health-metrics:testReleaseUnitTest &&
./gradlew :clickstream-health-metrics-noop:testReleaseUnitTest &&
./gradlew :clickstream-health-metrics-api:testReleaseUnitTest