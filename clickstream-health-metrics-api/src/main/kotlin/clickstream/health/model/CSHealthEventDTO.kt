package clickstream.health.model

import clickstream.health.constant.CSEventNamesConstant.ClickStreamEventBatchCreated
import clickstream.health.constant.CSHealthKeysConstant
import clickstream.health.constant.CSEventTypesConstant

public data class CSHealthEventDTO(
    val healthEventID: Int = 0,
    val eventName: String,
    val eventType: String,
    val timestamp: String = "",
    val eventId: String = "",
    val eventBatchId: String = "",
    val error: String = "",
    val sessionId: String = "",
    val count: Int = 0,
    val networkType: String = "",
    val startTime: Long = 0L,
    val stopTime: Long = 0L,
    val bucketType: String = "",
    val batchSize: Long = 0L,
    val appVersion: String
) {

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

        if (eventId.isNotBlank()) {
            eventData[getEventIdKey()] = eventId
        }

        if (eventBatchId.isNotBlank()) {
            eventData[getEvenBatchIdKey()] = eventBatchId
        }
        if (timestamp.isNotBlank()) {
            eventData[CSHealthKeysConstant.TIMESTAMP] = timestamp
        }

        if (count != 0) {
            eventData[CSHealthKeysConstant.COUNT] = count
        }

        if (bucketType.isNotBlank()) {
            eventData[CSHealthKeysConstant.BUCKET] = bucketType
        }

        return eventData
    }

    private fun getEventIdKey(): String =
        when (eventType) {
            CSEventTypesConstant.INSTANT -> {
                if (eventName == ClickStreamEventBatchCreated.value) {
                    CSHealthKeysConstant.EVENTS
                } else {
                    CSHealthKeysConstant.EVENT_ID
                }
            }
            CSEventTypesConstant.AGGREGATE -> CSHealthKeysConstant.EVENTS
            CSEventTypesConstant.BUCKET -> CSHealthKeysConstant.EVENTS
            else -> CSHealthKeysConstant.EVENTS
        }

    private fun getEvenBatchIdKey(): String =
        when (eventType) {
            CSEventTypesConstant.INSTANT -> CSHealthKeysConstant.EVENT_BATCH_ID
            CSEventTypesConstant.AGGREGATE -> CSHealthKeysConstant.EVENT_BATCHES
            CSEventTypesConstant.BUCKET -> CSHealthKeysConstant.EVENT_BATCHES
            else -> CSHealthKeysConstant.EVENT_BATCHES
        }
}
