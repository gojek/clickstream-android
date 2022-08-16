package clickstream.eventvisualiser.ui.internal.data.repository.fakes

import clickstream.eventvisualiser.ui.internal.data.datasource.CSEvDatasource
import clickstream.eventvisualiser.ui.internal.data.model.CSEvEvent
import clickstream.eventvisualiser.ui.internal.data.repository.CSEvRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

internal class FakeEvEventRepository(private val csEvDatasource: CSEvDatasource) :
    CSEvRepository {

    override val isConnected: Flow<Boolean>
        get() = csEvDatasource.isConnected

    override fun startObserving() {
        csEvDatasource.startObserving()
    }

    override fun stopObserving() {
        csEvDatasource.stopObserving()
    }

    override suspend fun getAllEventNames(
        keys: List<String>,
        values: List<String>
    ): List<String> {
        return csEvDatasource.getAllEventNames(keys, values)
    }

    override suspend fun getEventByNameAndId(eventId: String, eventName: String): CSEvEvent? {
        return csEvDatasource.getEventByNameAndId(eventId, eventName)
    }

    override suspend fun getEventDetailList(eventName: String): List<CSEvEvent> {
        return csEvDatasource.getEventDetailList(eventName)
    }

    override suspend fun getEventProperties(
        eventName: String,
        eventId: String
    ): Map<String, Any?> {
        return csEvDatasource.getEventProperties(eventName, eventId)
    }

    override suspend fun getFilteredList(key: String): List<String> =
        withContext(Dispatchers.Default) {
            val properKey = key.trim().toLowerCase()
            return@withContext csEvDatasource.getAllEventNames()
                .filter { it.toLowerCase().contains(properKey) }
        }

    override suspend fun getEventsFilteredByProperty(key: String, value: String): List<String> {
        return csEvDatasource.getAllEventNames()
    }

    override suspend fun clearData() {
        csEvDatasource.clearData()
    }
}