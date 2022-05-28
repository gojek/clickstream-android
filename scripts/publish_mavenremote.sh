#!/usr/bin/env bash

./gradlew :clickstream:assembleRelease -PisHealthEnabled=true && ./gradlew :clickstream:publishReleasePublicationToSonatypeRepository --max-workers 1 closeAndReleaseSonatypeStagingRepository

