package clickstream.health.model

import clickstream.health.constant.CSTrackedVia

/**
 * Config for HealthEventTracker, based on which the health events are handled
 *
 * @param minimumTrackedVersion - The minimum app version above which the event will be sent.
 * @param randomisingUserIdRemainders - A list with the last char of userID for whom the health events are tracked.
 */
public data class CSHealthEventConfig(
    val minimumTrackedVersion: String,
    val randomisingUserIdRemainders: List<Int>,
    val trackedVia: CSTrackedVia
) {
    /**
     * Checking whether the current app version is greater than the version in the config.
     */
    private fun isAppVersionGreater(appVersion: String): Boolean {
        return appVersion.isNotBlank() &&
                minimumTrackedVersion.isNotBlank() &&
                convertVersionToNumber(appVersion) >= convertVersionToNumber(minimumTrackedVersion)
    }

    /**
     * Checking whether the userID is present in the randomUserIdRemainder list
     */
    private fun isRandomUser(userId: Int): Boolean {
        return randomisingUserIdRemainders.isNotEmpty() && randomisingUserIdRemainders.contains(userId % DIVIDING_FACTOR)
    }

    /**
     * Checking whether the user is on alpha
     */
    private fun isAlpha(appVersion: String): Boolean {
        return appVersion.contains(ALPHA, true)
    }

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

    public fun isTrackedForExternal(): Boolean {
        return trackedVia == CSTrackedVia.External
    }

    public fun isTrackedForInternal(): Boolean {
        return trackedVia == CSTrackedVia.Internal
    }

    public fun isTrackedForBoth(): Boolean {
        return trackedVia == CSTrackedVia.Both
    }

    public companion object {
        private const val DIVIDING_FACTOR: Int = 10
        private const val MULTIPLICATION_FACTOR: Int = 10
        private const val ALPHA: String = "alpha"

        public const val MAX_VERBOSITY_LEVEL: String = "maximum"

        /**
         * Creates the default instance of the config
         */
        public fun default(trackedVia: CSTrackedVia): CSHealthEventConfig = CSHealthEventConfig(
            minimumTrackedVersion = "",
            randomisingUserIdRemainders = emptyList(),
            trackedVia = trackedVia
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
