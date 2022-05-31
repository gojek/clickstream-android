package clickstream.fake

import clickstream.health.model.CSHealthEventConfig
import clickstream.health.model.CSHealthEventConfig.Companion.MAX_VERBOSITY_LEVEL

internal val fakeCSHealthEventConfig = CSHealthEventConfig(
    minTrackedVersion = "4.37.0",
    randomUserIdRemainder = listOf(123453, 5),
    destination = emptyList(),
    verbosityLevel = MAX_VERBOSITY_LEVEL
)