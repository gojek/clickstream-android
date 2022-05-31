#!/usr/bin/env bash

./gradlew :clickstream:apiDump &&
./gradlew :clickstream-api:apiDump &&
./gradlew :clickstream-logger:apiDump &&
./gradlew :clickstream-lifecycle:apiDump &&
./gradlew :clickstream-util:apiDump &&
./gradlew :clickstream-health-metrics:apiDump &&
./gradlew :clickstream-health-metrics-noop:apiDump &&
./gradlew :clickstream-health-metrics-api:apiDump