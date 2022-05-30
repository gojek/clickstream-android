package clickstream.health.model

import clickstream.health.model.CSEventNames.ClickStreamEventBatchCreated

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
            eventData[CSHealthKeys.EVENT_TYPE] = eventType
        }

        if (sessionId.isNotBlank()) {
            eventData[CSHealthKeys.SESSION_ID] = sessionId
        }

        if (error.isNotBlank()) {
            eventData[CSHealthKeys.REASON] = error
        }

        if (eventId.isNotBlank()) {
            eventData[getEventIdKey()] = eventId
        }

        if (eventBatchId.isNotBlank()) {
            eventData[getEvenBatchIdKey()] = eventBatchId
        }
        if (timestamp.isNotBlank()) {
            eventData[CSHealthKeys.TIMESTAMP] = timestamp
        }

        if (count != 0) {
            eventData[CSHealthKeys.COUNT] = count
        }

        if (bucketType.isNotBlank()) {
            eventData[CSHealthKeys.BUCKET] = bucketType
        }

        return eventData
    }

    private fun getEventIdKey(): String =
        when (eventType) {
            EventTypes.INSTANT -> {
                if (eventName == ClickStreamEventBatchCreated.value) {
                    CSHealthKeys.EVENTS
                } else {
                    CSHealthKeys.EVENT_ID
                }
            }
            EventTypes.AGGREGATE -> CSHealthKeys.EVENTS
            EventTypes.BUCKET -> CSHealthKeys.EVENTS
            else -> CSHealthKeys.EVENTS
        }

    private fun getEvenBatchIdKey(): String =
        when (eventType) {
            EventTypes.INSTANT -> CSHealthKeys.EVENT_BATCH_ID
            EventTypes.AGGREGATE -> CSHealthKeys.EVENT_BATCHES
            EventTypes.BUCKET -> CSHealthKeys.EVENT_BATCHES
            else -> CSHealthKeys.EVENT_BATCHES
        }
}
