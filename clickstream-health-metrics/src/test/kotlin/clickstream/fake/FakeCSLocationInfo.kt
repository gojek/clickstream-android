package clickstream.fake

import clickstream.api.CSLocationInfo

internal val fakeLocationInfo = CSLocationInfo(
    longitude = 10.00,
    latitude = 12.00,
    s2Ids = mapOf("key" to "value")
)