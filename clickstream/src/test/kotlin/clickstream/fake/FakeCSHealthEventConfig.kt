package clickstream.fake

import clickstream.health.model.CSHealthEventConfig

internal val fakeCSHealthEventConfig = CSHealthEventConfig(
    minTrackedVersion = "1.0.0",
    randomUserIdRemainder = listOf(),
    destination = listOf("CS", "CT"),
    verbosityLevel = "min"
)