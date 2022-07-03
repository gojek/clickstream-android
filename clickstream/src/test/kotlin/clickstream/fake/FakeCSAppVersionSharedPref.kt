package clickstream.fake

import clickstream.util.CSAppVersionSharedPref

internal class FakeCSAppVersionSharedPref(
    private val value: Boolean
) : CSAppVersionSharedPref {
    override suspend fun isAppVersionEqual(currentAppVersion: String): Boolean {
        return value
    }
}