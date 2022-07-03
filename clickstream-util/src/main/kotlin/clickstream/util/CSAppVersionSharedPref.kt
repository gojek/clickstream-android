package clickstream.util

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface CSAppVersionSharedPref {
    public suspend fun isAppVersionEqual(currentAppVersion: String): Boolean
}
