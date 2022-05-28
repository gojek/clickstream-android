package com.clickstream.app.config

fun CSInfo() = CSInfo(
    appInfo = CSAppInfo(appVersion = "1.1.0"),
    locationInfo = CSLocationInfo(
        latitude = -6.1753871,
        longitude = 106.8249641,
        s2Ids = emptyMap()
    ),
    sessionInfo = CSSessionInfo("1234"),
    deviceInfo = CSDeviceInfo(),
    customerInfo = CSUserInfo(
        "ID", "ID", 1234, "bill@gmail.com"
    )
)
