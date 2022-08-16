package clickstream.eventvisualiser.ui.internal.data.datasource

import clickstream.eventvisualiser.ui.internal.data.model.CSEvEvent
import kotlinx.coroutines.flow.Flow

internal interface CSEvDatasource {

    val isConnected: Flow<Boolean>

    fun startObserving()

    fun stopObserving()

    suspend fun addEvents(eventList: List<CSEvEvent>)

    suspend fun getAllEventNames(
        keys: List<String> = listOf(),
        values: List<String> = listOf()
    ): List<String>

    suspend fun getEventByNameAndId(eventId: String, eventName: String): CSEvEvent?

    suspend fun getEventDetailList(eventName: String): List<CSEvEvent>

    suspend fun getEventProperties(eventName: String, eventId: String): Map<String, Any?>

    suspend fun clearData()
}