package clickstream.internal.db.impl

import android.content.Context
import clickstream.internal.db.CSAppVersionSharedPref
import kotlinx.coroutines.coroutineScope

private const val CLICKSTREAM_PREF = "Clickstream_Version_Pref"
private const val APP_VERSION_KEY = "app_version"

/**
 * This class maintains app version state in Shared Preference
 */
internal class DefaultCSAppVersionSharedPref(
    private val context: Context
) : CSAppVersionSharedPref {

    /**
     * This method checks if app has been updated or not by comparing with previous app version
     * */
    override suspend fun isAppVersionEqual(currentAppVersion: String): Boolean {
        return coroutineScope {
            val sharedPref =
                context.getSharedPreferences(CLICKSTREAM_PREF, Context.MODE_PRIVATE)
            val oldAppVersion = sharedPref.getString(APP_VERSION_KEY, "")
            when {
                oldAppVersion == "" -> {
                    saveAppVersion(currentAppVersion)
                    true
                }
                oldAppVersion != currentAppVersion -> {
                    saveAppVersion(currentAppVersion)
                    false
                }
                else -> {
                    currentAppVersion == oldAppVersion
                }
            }
        }
    }

    private suspend fun saveAppVersion(appVersion: String) {
        coroutineScope {
            val sharedPref =
                context.getSharedPreferences(CLICKSTREAM_PREF, Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString(APP_VERSION_KEY, appVersion)
                apply()
            }
        }
    }
}
