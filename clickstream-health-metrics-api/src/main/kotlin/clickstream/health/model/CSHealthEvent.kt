package clickstream.health.model

import androidx.annotation.RestrictTo
import clickstream.health.constant.CSHealthKeysConstant
import clickstream.health.intermediate.CSHealthEventProcessor


/***
 * Actual health object used by [CSHealthEventProcessor].
 *
 * */
public data class CSHealthEvent(
    val healthEventID: Int = 0,
    val eventName: String,
    val eventType: String,
    val timestamp: String = "",
    val eventGuid: String = "",
    val eventBatchGuid: String = "",
    val error: String = "",
    val sessionId: String = "",
    val count: Int = 0,
    val networkType: String = "",
    val batchSize: Long = 0L,
    val appVersion: String,
) {

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public fun eventData(): HashMap<String, Any> {
        val eventData: HashMap<String, Any> = hashMapOf()

        if (eventType.isNotBlank()) {
            eventData[CSHealthKeysConstant.EVENT_TYPE] = eventType
        }

        if (sessionId.isNotBlank()) {
            eventData[CSHealthKeysConstant.SESSION_ID] = sessionId
        }

        if (error.isNotBlank()) {
            eventData[CSHealthKeysConstant.REASON] = error
        }

        if (eventGuid.isNotBlank()) {
            eventData[CSHealthKeysConstant.EVENT_ID] = eventGuid
        }

        if (eventBatchGuid.isNotBlank()) {
            eventData[CSHealthKeysConstant.EVENT_BATCH_ID] = eventBatchGuid
        }
        if (timestamp.isNotBlank()) {
            eventData[CSHealthKeysConstant.TIMESTAMP] = timestamp
        }

        if (count != 0) {
            eventData[CSHealthKeysConstant.COUNT] = count
        }

        if (appVersion.isNotBlank()) {
            eventData[CSHealthKeysConstant.APP_VERSION] = appVersion
        }

        if (batchSize != 0L) {
            eventData[CSHealthKeysConstant.EVENT_BATCHES] = batchSize
        }

        return eventData
    }
}
