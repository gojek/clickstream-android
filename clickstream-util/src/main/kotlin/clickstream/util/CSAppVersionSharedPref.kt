package clickstream.util

public interface CSAppVersionSharedPref {
    public suspend fun isAppVersionEqual(currentAppVersion: String): Boolean
}
