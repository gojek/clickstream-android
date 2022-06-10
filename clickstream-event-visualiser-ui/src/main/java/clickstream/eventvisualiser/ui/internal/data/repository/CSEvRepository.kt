package clickstream.eventvisualiser.ui.internal.data.repository

import clickstream.eventvisualiser.ui.internal.data.model.CSEvEvent

internal interface CSEvRepository {

    fun startObserving()

    fun stopObserving()

    suspend fun getAllEventNames(
        keys: List<String> = listOf(),
        values: List<String> = listOf(),
    ): List<String>

    suspend fun getEventDetailList(eventName: String): List<CSEvEvent>

    suspend fun getEventProperties(eventName: String, eventId: String): Map<String, Any?>

    suspend fun getFilteredList(key: String): List<String>

    suspend fun getEventsFilteredByProperty(key: String, value: String): List<String>

    suspend fun clearData()
}