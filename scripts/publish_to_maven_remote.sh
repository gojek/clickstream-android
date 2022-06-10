#!/usr/bin/env bash

./gradlew :clickstream:assembleRelease && ./gradlew :clickstream:publishReleasePublicationToSonatypeRepository --max-workers 1 closeAndReleaseSonatypeStagingRepository
./gradlew :clickstream-api:assembleRelease && ./gradlew :clickstream-api:publishReleasePublicationToSonatypeRepository --max-workers 1 closeAndReleaseSonatypeStagingRepository
./gradlew :clickstream-logger:assembleRelease && ./gradlew :clickstream-logger:publishReleasePublicationToSonatypeRepository --max-workers 1 closeAndReleaseSonatypeStagingRepository
./gradlew :clickstream-lifecycle:assembleRelease && ./gradlew :clickstream-lifecycle:publishReleasePublicationToSonatypeRepository --max-workers 1 closeAndReleaseSonatypeStagingRepository
./gradlew :clickstream-util:assembleRelease && ./gradlew :clickstream-util:publishReleasePublicationToSonatypeRepository --max-workers 1 closeAndReleaseSonatypeStagingRepository
./gradlew :clickstream-health-metrics:assembleRelease && ./gradlew :clickstream-health-metrics:publishReleasePublicationToSonatypeRepository --max-workers 1 closeAndReleaseSonatypeStagingRepository
./gradlew :clickstream-health-metrics-noop:assembleRelease && ./gradlew :clickstream-health-metrics-noop:publishReleasePublicationToSonatypeRepository --max-workers 1 closeAndReleaseSonatypeStagingRepository
./gradlew :clickstream-health-metrics-api:assembleRelease && ./gradlew :clickstream-health-metrics-api:publishReleasePublicationToSonatypeRepository --max-workers 1 closeAndReleaseSonatypeStagingRepository
./gradlew :clickstream-event-visualiser:assembleRelease && ./gradlew :clickstream-event-visualiser:publishReleasePublicationToSonatypeRepository --max-workers 1 closeAndReleaseSonatypeStagingRepository
./gradlew :clickstream-event-interceptor:assemble && ./gradlew :clickstream-event-interceptor:publishReleasePublicationToSonatypeRepository --max-workers 1 closeAndReleaseSonatypeStagingRepository
./gradlew :clickstream-event-visualiser-ui:assembleRelease && ./gradlew :clickstream-event-visualiser-ui:publishReleasePublicationToSonatypeRepository --max-workers 1 closeAndReleaseSonatypeStagingRepository
./gradlew :clickstream-event-visualiser-noop:assembleRelease && ./gradlew :clickstream-event-visualiser-noop:publishReleasePublicationToSonatypeRepository --max-workers 1 closeAndReleaseSonatypeStagingRepository
./gradlew :clickstream-event-visualiser-ui-noop:assembleRelease && ./gradlew :clickstream-event-visualiser-ui-noop:publishReleasePublicationToSonatypeRepository --max-workers 1 closeAndReleaseSonatypeStagingRepository
