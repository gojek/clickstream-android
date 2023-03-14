package clickstream.eventvisualiser.ui.internal.data.model

internal data class CSEvEvent(
    val eventName: String,
    val eventId: String,
    val timeStampInMillis: Long,
    val properties: Map<String, Any?>,
    val state: CSEvState,
)