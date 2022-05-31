#!/usr/bin/env bash

./gradlew :clickstream:check &&
./gradlew :clickstream-api:check &&
./gradlew :clickstream-logger:check &&
./gradlew :clickstream-lifecycle:check &&
./gradlew :clickstream-util:check &&
./gradlew :clickstream-health-metrics:check &&
./gradlew :clickstream-health-metrics-noop:check &&
./gradlew :clickstream-health-metrics-api:check