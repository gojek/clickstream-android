package clickstream.fake

import clickstream.CSDeviceInfo
import clickstream.CSInfo

internal fun fakeCSInfo(
    deviceInfo: CSDeviceInfo? = null
) = CSInfo(
    deviceInfo = deviceInfo ?: fakeDeviceInfo(),
    userInfo = fakeUserInfo(),
    locationInfo = fakeLocationInfo,
    appInfo = fakeAppInfo,
    sessionInfo = fakeCSSessionInfo
)