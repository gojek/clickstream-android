package clickstream.config

import clickstream.internal.analytics.CSEventNames
import java.util.Locale

private const val DIVIDING_FACTOR: Int = 10
private const val MULTIPLICATION_FACTOR: Int = 10
internal const val MAX_VERBOSITY_LEVEL: String = "maximum"
public const val CS_DESTINATION: String = "CS"
public const val CT_DESTINATION: String = "CT"
public const val ALPHA: String = "alpha"

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
    val verbosityLevel: String
) {
    /**
     * Checking whether the current app version is greater than the version in the config.
     */
    private fun isAppVersionGreater(appVersion: String): Boolean =
        appVersion.isNotBlank() && minTrackedVersion.isNotBlank() &&
                convertVersionToNumber(appVersion) >= convertVersionToNumber(minTrackedVersion)

    /**
     * Checking whether the userID is present in the randomUserIdRemainder list
     */
    private fun isRandomUser(userId: Int): Boolean {
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
    public fun isEnabled(appVersion: String, userId: Int): Boolean {
        return isAppVersionGreater(appVersion) && (isRandomUser(userId) || isAlpha(appVersion))
    }

    /**
     * With the given event name, it returns whether the event should be sent to Clickstream or not.
     *
     * @param eventName - Current event name
     *
     * @return Boolean - True if should be sent to CS
     */
    public fun isTrackedViaClickstream(eventName: String): Boolean =
        listOf(
            CSEventNames.ClickStreamEventReceived.value,
            CSEventNames.ClickStreamEventObjectCreated.value,
            CSEventNames.ClickStreamEventCached.value,
            CSEventNames.ClickStreamEventBatchCreated.value,
            CSEventNames.ClickStreamBatchSent.value,
            CSEventNames.ClickStreamEventBatchAck.value,
            CSEventNames.ClickStreamFlushOnBackground.value
        ).contains(eventName)

    /**
     * Returns whether logging level is set to Maximum or not
     *
     * @return Boolean - True if the condition satisfies else false
     */
    public fun isVerboseLoggingEnabled(): Boolean =
        verbosityLevel.toLowerCase(Locale.getDefault()) == MAX_VERBOSITY_LEVEL

    public companion object {
        /**
         * Creates the default instance of the config
         */
        public fun default(): CSHealthEventConfig = CSHealthEventConfig(
            minTrackedVersion = "",
            randomUserIdRemainder = emptyList(),
            destination = emptyList(),
            verbosityLevel = ""
        )

        /**
         * Converts the app version to the integer format.
         * For example,if the app version is "1.2.1.beta1", it's
         * converted as "121"
         */
        private fun convertVersionToNumber(version: String): Int {
            var versionNum: Int = 0
            version.split(".").asIterable()
                .filter { it.matches("-?\\d+(\\.\\d+)?".toRegex()) }
                .map {
                    versionNum = (versionNum * MULTIPLICATION_FACTOR) + it.toInt()
                }
            return versionNum
        }
    }
}
