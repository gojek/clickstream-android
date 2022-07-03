package clickstream.fake

import clickstream.api.CSAppInfo
import clickstream.api.CSDeviceInfo
import clickstream.api.CSInfo
import clickstream.api.CSLocationInfo
import clickstream.api.CSMetaProvider
import clickstream.api.CSSessionInfo
import clickstream.api.CSUserInfo
import kotlinx.coroutines.runBlocking

internal fun fakeCSInfo(
    csMetaProvider: CSMetaProvider? = null,
    deviceInfo: CSDeviceInfo? = null
) = CSInfo(
    deviceInfo = deviceInfo ?: buildDeviceInfo(csMetaProvider),
    userInfo = buildUserInfo(csMetaProvider),
    locationInfo = buildLocationInfo(csMetaProvider),
    appInfo = buildAppInfo(csMetaProvider),
    sessionInfo = buildSessionInfo(csMetaProvider)
)

private fun buildDeviceInfo(csMetaProvider: CSMetaProvider?): CSDeviceInfo {
    return if (csMetaProvider != null) {
        object : CSDeviceInfo {
            override fun getDeviceManufacturer(): String = csMetaProvider.device.deviceMake
            override fun getDeviceModel(): String = csMetaProvider.device.deviceModel
            override fun getSDKVersion(): String = csMetaProvider.device.operatingSystemVersion
            override fun getOperatingSystem(): String = csMetaProvider.device.operatingSystem
            override fun getDeviceHeight(): String = "1"
            override fun getDeviceWidth(): String = "2"
        }
    } else {
        fakeDeviceInfo()
    }
}


private fun buildSessionInfo(csMetaProvider: CSMetaProvider?): CSSessionInfo {
    return if (csMetaProvider != null) {
        CSSessionInfo(sessionID = csMetaProvider.session.sessionId)
    } else {
        fakeCSSessionInfo
    }
}

private fun buildAppInfo(csMetaProvider: CSMetaProvider?): CSAppInfo {
    return if (csMetaProvider != null) {
        CSAppInfo(appVersion = csMetaProvider.app.version)
    } else {
        fakeAppInfo
    }
}

private fun buildLocationInfo(csMetaProvider: CSMetaProvider?): CSLocationInfo {
    return if (csMetaProvider != null) {
        runBlocking {
            CSLocationInfo(
                latitude = csMetaProvider.location().latitude,
                longitude = csMetaProvider.location().longitude,
                s2Ids = emptyMap()
            )
        }
    } else {
        fakeLocationInfo
    }
}

private fun buildUserInfo(csMetaProvider: CSMetaProvider?): CSUserInfo {
    return if (csMetaProvider != null) {
        CSUserInfo(
            currentCountry = csMetaProvider.customer.currentCountry,
            signedUpCountry = csMetaProvider.customer.signedUpCountry,
            identity = csMetaProvider.customer.identity,
            email = csMetaProvider.customer.email
        )
    } else {
        fakeUserInfo()
    }
}