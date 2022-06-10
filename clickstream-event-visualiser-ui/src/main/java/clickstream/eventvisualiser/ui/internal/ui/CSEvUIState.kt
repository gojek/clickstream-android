package clickstream.eventvisualiser.ui.internal.ui

internal data class CSEvHomeUIState(
    override val events: List<CSEvListItem> = listOf(),
    val originalList: List<CSEvListItem> = listOf(),
    val isFilterApplied: Boolean = false,
    override val header: String = "Event Visualiser"
) : CSEvUIState(events, header)

internal open class CSEvUIState(
    open val events: List<CSEvListItem> = listOf(),
    open val header: String = ""
)