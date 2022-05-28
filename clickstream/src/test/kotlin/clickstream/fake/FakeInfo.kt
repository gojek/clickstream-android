package clickstream.fake

import clickstream.CSAppInfo
import clickstream.CSDeviceInfo
import clickstream.CSInfo
import clickstream.CSLocationInfo
import clickstream.CSSessionInfo
import clickstream.CSUserInfo

internal fun fakeInfo(): CSInfo {
    return CSInfo(
        appInfo = fakeAppInfo(),
        locationInfo = fakeLocationInfo(),
        sessionInfo = fakeSessionInfo(),
        deviceInfo = fakeDeviceInfo(),
        userInfo = fakeUserInfo()
    )
}

internal fun fakeAppInfo(
    appVersion: String = "1"
): CSAppInfo {
    return CSAppInfo(appVersion)
}

internal fun fakeLocationInfo(
    userLatitude: Double = -6.1753924,
    userLongitude: Double = 106.8249641,
    s2Ids: Map<String, String> = emptyMap()
): CSLocationInfo {
    return CSLocationInfo(userLatitude, userLongitude, s2Ids)
}

internal fun fakeUserInfo(
    currentCountry: String = "ID",
    signedUpCountry: String = "ID",
    identity: Int = 12345,
    email: String = "test@gmail.com"
): CSUserInfo {
    return CSUserInfo(currentCountry, signedUpCountry, identity, email)
}

internal fun fakeSessionInfo(
    sessionId: String = "123456"
): CSSessionInfo {
    return CSSessionInfo(sessionId)
}

internal fun fakeDeviceInfo(): CSDeviceInfo {

    return object : CSDeviceInfo {
        override fun getDeviceManufacturer(): String = "Samsung"
        override fun getDeviceModel(): String = "IPhone X"
        override fun getSDKVersion(): String = "15"
        override fun getOperatingSystem(): String = "IOS"
        override fun getDeviceHeight(): String = "1024"
        override fun getDeviceWidth(): String = "400"
    }
}
