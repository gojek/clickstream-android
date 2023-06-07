package com.clickstream.app.config

import clickstream.api.CSAppInfo
import clickstream.api.CSInfo
import clickstream.api.CSLocationInfo
import clickstream.api.CSSessionInfo
import clickstream.api.CSUserInfo

fun csInfo() = CSInfo(
    appInfo = CSAppInfo(appVersion = "2.1.0"),
    locationInfo = CSLocationInfo(
        latitude = -6.1753871,
        longitude = 106.8249641,
        s2Ids = emptyMap()
    ),
    sessionInfo = CSSessionInfo("1234"),
    deviceInfo = csDeviceInfo(),
    userInfo = CSUserInfo(
        "ID", "ID", 1234, "bill@gmail.com"
    )
)
