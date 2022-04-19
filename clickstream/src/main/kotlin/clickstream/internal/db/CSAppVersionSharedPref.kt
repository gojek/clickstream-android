package clickstream.internal.db

internal interface CSAppVersionSharedPref {
    suspend fun isAppVersionEqual(currentAppVersion: String): Boolean
}
