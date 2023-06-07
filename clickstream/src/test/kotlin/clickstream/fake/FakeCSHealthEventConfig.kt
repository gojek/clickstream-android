package clickstream.fake

import clickstream.health.constant.CSTrackedVia
import clickstream.health.model.CSHealthEventConfig

internal val fakeCSHealthEventConfig = CSHealthEventConfig(
    minimumTrackedVersion = "1.0.0",
    randomisingUserIdRemainders = listOf(),
    CSTrackedVia.Both
)