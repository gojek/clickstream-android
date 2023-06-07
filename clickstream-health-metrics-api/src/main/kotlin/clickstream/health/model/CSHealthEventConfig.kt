package clickstream.health.model

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import clickstream.health.constant.CSHealthEventName
import java.util.Locale

private const val DIVIDING_FACTOR: Int = 10
internal const val MAX_VERBOSITY_LEVEL: String = "maximum"
public const val INTERNAL: String = "CS"
public const val EXTERNAL: String = "CT"
public const val ALPHA: String = "alpha"

private val internalEventList = listOf(
    CSHealthEventName.ClickStreamEventBatchAck.value,
    CSHealthEventName.ClickStreamBatchSent.value,
    CSHealthEventName.ClickStreamFlushOnBackground.value,
    CSHealthEventName.ClickStreamFlushOnForeground.value
)

private val externalEventList = listOf(
    CSHealthEventName.ClickStreamEventBatchTimeout.value,
    CSHealthEventName.ClickStreamConnectionFailed.value,
    CSHealthEventName.ClickStreamEventBatchErrorResponse.value,
    CSHealthEventName.ClickStreamBatchWriteFailed.value,
    CSHealthEventName.ClickStreamEventBatchTriggerFailed.value
)

/**
 * Config for HealthEventTracker, based on which the health events are handled
 *
 * @param minTrackedVersion - The minimum app version above which the event will be sent.
 * @param randomUserIdRemainder - A list with the last char of userID for whom the health events are tracked.
 */
public data class CSHealthEventConfig(
    val minTrackedVersion: String,
    val randomUserIdRemainder: List<Int>,
    val destination: List<String>,
    val verbosityLevel: String,
) {

    /**
     * Checking whether the current app version is greater than the version in the config.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public fun isAppVersionGreater(appVersion: String, minAppVersion: String): Boolean {
        var appVersionIterator = 0
        var minAppVersionIterator = 0
        var isAppVersionGreater = false

        while (appVersionIterator < appVersion.length && minAppVersionIterator < minAppVersion.length) {
            if (appVersion[appVersionIterator] > minAppVersion[minAppVersionIterator]) {
                isAppVersionGreater = true
                break
            } else if (appVersion[appVersionIterator] < minAppVersion[minAppVersionIterator]) {
                isAppVersionGreater = false
                break
            } else {
                appVersionIterator++
                minAppVersionIterator++
            }
        }
        return isAppVersionGreater
    }

    /**
     * Checking whether the userID is present in the randomUserIdRemainder list
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public fun isHealthEnabledUser(userId: Int): Boolean {
        return randomUserIdRemainder.isNotEmpty() && randomUserIdRemainder.contains(userId % DIVIDING_FACTOR)
    }

    /**
     * Checking whether the user is on alpha
     */
    private fun isAlpha(appVersion: String): Boolean =
        appVersion.contains(ALPHA, true)

    /**
     * With the given app version and user ID, it is
     * compared with the values in the config and the value is returned
     *
     * @param appVersion - Current app version
     * @param userId - current user id
     *
     * @return Boolean - True if the condition satisfies else false
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public fun isEnabled(appVersion: String, userId: Int): Boolean {
        return isAppVersionGreater(
            appVersion, minTrackedVersion
        ) && (isHealthEnabledUser(userId) || isAlpha(appVersion))
    }

    /**
     * Returns whether logging level is set to Maximum or not
     *
     * @return Boolean - True if the condition satisfies else false
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public fun isVerboseLoggingEnabled(): Boolean =
        verbosityLevel.lowercase(Locale.getDefault()) == MAX_VERBOSITY_LEVEL

    public companion object {
        /**
         * Creates the default instance of the config
         */
        public fun default(): CSHealthEventConfig = CSHealthEventConfig(
            minTrackedVersion = "",
            randomUserIdRemainder = emptyList(),
            destination = emptyList(),
            verbosityLevel = "",
        )

        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        public fun isTrackedViaInternal(eventName: String): Boolean {
            return internalEventList.contains(eventName)
        }

        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        public fun isTrackedViaExternal(eventName: String): Boolean {
            return externalEventList.contains(eventName)
        }
    }
}
