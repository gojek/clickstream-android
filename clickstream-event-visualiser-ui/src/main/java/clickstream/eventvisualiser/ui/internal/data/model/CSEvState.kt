package clickstream.eventvisualiser.ui.internal.data.model

internal enum class CSEvState(public val tag: String) {
    SCHEDULED("Scheduled"),
    ACKNOWLEDGED("Acknowledged"),
    DISPATCHED("Dispatched")
}