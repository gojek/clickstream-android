package clickstream.fake

import clickstream.config.CSDeviceInfo
import clickstream.model.CSInfo

internal fun fakeCSInfo(
    deviceInfo: CSDeviceInfo? = null
) = CSInfo(
    deviceInfo = deviceInfo ?: fakeDeviceInfo(),
    customerInfo = fakeCustomerInfo,
    locationInfo = fakeLocationInfo,
    appInfo = fakeAppInfo,
    sessionInfo = fakeCSSessionInfo
)
