package clickstream.fake

import clickstream.health.model.CSHealthEventConfig

internal val fakeCSHealthEventConfig = CSHealthEventConfig(
    minTrackedVersion = "4.37.0",
    randomUserIdRemainder = listOf(123453, 5),
    destination = emptyList(),
    verbosityLevel = "maximum",
)