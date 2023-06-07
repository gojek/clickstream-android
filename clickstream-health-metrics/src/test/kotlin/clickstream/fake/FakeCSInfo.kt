package clickstream.fake

import clickstream.api.CSDeviceInfo
import clickstream.api.CSInfo

internal fun fakeCSInfo(
    deviceInfo: CSDeviceInfo? = null
) = CSInfo(
    deviceInfo = deviceInfo ?: fakeDeviceInfo(),
    userInfo = fakeUserInfo(),
    locationInfo = fakeLocationInfo,
    appInfo = fakeAppInfo,
    sessionInfo = fakeCSSessionInfo
)