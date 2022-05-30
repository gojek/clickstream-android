package clickstream.health

public interface CSAppVersionSharedPref {
    public suspend fun isAppVersionEqual(currentAppVersion: String): Boolean
}
