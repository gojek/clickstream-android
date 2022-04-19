package clickstream.model

import clickstream.config.CSDeviceInfo

/**
 * A common data class which wraps [CSAppInfo], [CSLocationInfo],
 * [CSUserInfo], [CSSessionInfo], [CSDeviceInfo]
 */
public data class CSInfo(
    val appInfo: CSAppInfo,
    val locationInfo: CSLocationInfo,
    val sessionInfo: CSSessionInfo,
    val deviceInfo: CSDeviceInfo,
    val customerInfo: CSUserInfo
)

/**
 * Data class which holds app details.
 *
 * @param appVersion
 */
public data class CSAppInfo(
    val appVersion: String
)

/**
 * Data class which holds location details.
 *
 * @param longitude
 * @param latitude
 * @param s2Ids
 */
public data class CSLocationInfo(
    val latitude: Double,
    val longitude: Double,
    val s2Ids: Map<String, String>
) {

    /**
     * Inner class which wraps the latitude and longitude
     */
    public data class Location(val latitude: Double, val longitude: Double)
}

/**
 * Data class which holds customer details.
 *
 * @param currentCountry
 * @param signedUpCountry
 * @param identity a user id
 * @param email
 */
public data class CSUserInfo(
    val currentCountry: String,
    val signedUpCountry: String,
    val identity: Int,
    val email: String
)

/**
 * Data class which holds session details.
 *
 * @param sessionID
 */
public data class CSSessionInfo(
    val sessionID: String
)
