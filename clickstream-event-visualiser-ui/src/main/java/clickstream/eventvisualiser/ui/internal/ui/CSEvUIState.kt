package clickstream.eventvisualiser.ui.internal.ui

internal data class CSEvHomeUIState(
    override val events: List<CSEvListItem> = listOf(),
    override val originalList: List<CSEvListItem> = listOf(),
    val isFilterApplied: Boolean = false,
    override val header: String = "Event Visualiser",
    val isConnected: Boolean = false
) : CSEvUIState(originalList, events, header)


internal data class CSEvDetailUIState(
    override val originalList: List<CSEvListItem> = listOf(),
    override val events: List<CSEvListItem> = listOf(),
    override val header: String = "",
) : CSEvUIState(originalList, events, header)

internal open class CSEvUIState(
    open val originalList: List<CSEvListItem> = listOf(),
    open val events: List<CSEvListItem> = listOf(),
    open val header: String = "",
)