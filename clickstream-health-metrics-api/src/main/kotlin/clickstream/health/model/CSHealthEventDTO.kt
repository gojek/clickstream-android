package clickstream.health.model

public data class CSHealthEventDTO(
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
    val startTime: Long = 0L,
    val stopTime: Long = 0L,
    val bucketType: String = "",
    val batchSize: Long = 0L,
    val appVersion: String,
    val timeToConnection: Long = 0L
)
