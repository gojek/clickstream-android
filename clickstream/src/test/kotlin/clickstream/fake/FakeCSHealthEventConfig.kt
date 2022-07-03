package clickstream.fake

import clickstream.health.constant.CSTrackedVia
import clickstream.health.model.CSHealthEventConfig

internal val fakeCSHealthEventConfig = CSHealthEventConfig(
    minimumTrackedVersion = "4.37.0",
    randomisingUserIdRemainders = listOf(123453, 5),
    trackedVia = CSTrackedVia.Both
)