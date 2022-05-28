package clickstream.fake

import clickstream.config.CSHealthEventConfig
import clickstream.config.MAX_VERBOSITY_LEVEL

internal val fakeCSHealthEventConfig = CSHealthEventConfig(
    minTrackedVersion = "4.37.0",
    randomUserIdRemainder = listOf(123453, 5),
    destination = emptyList(),
    verbosityLevel = MAX_VERBOSITY_LEVEL
)