#!/usr/bin/env bash

./gradlew :clickstream:assembleRelease -PisHealthEnabled=true && ./gradlew :clickstream:publishToMavenLocal