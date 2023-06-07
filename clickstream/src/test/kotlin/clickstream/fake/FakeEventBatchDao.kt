package clickstream.fake

import clickstream.internal.eventscheduler.CSEventData
import clickstream.internal.eventscheduler.CSEventDataDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Fake EventBatchDao which mocks the EventBatchDao
 * and has its own implementation
 *
 * @param dispatcher - The dispatcher on which the items are emitted
 */
public class FakeEventBatchDao(
    private val dispatcher: CoroutineDispatcher
) : CSEventDataDao {

    private val items = mutableListOf<CSEventData>()

    override fun loadAll(): Flow<List<CSEventData>> = flow {
        while (true) {
            emit(items)
            delay(1000)
        }
    }

    override suspend fun getAll(): List<CSEventData> {
        return items
    }

    override suspend fun loadOnGoingEvents(): List<CSEventData> =
        items.filter { it.isOnGoing }

    override suspend fun insert(eventData: CSEventData) {
        items.add(eventData)
    }

    override suspend fun insertAll(eventDataList: List<CSEventData>) {
        items.addAll(eventDataList)
    }

    override suspend fun deleteByGuId(eventBatchGuId: String) {
        items.removeIf { it.eventRequestGuid == eventBatchGuId }
    }

    override suspend fun setOnGoingEvent(guid: String, ongoing: Boolean) {
        val newList = items.map { it.copy(isOnGoing = ongoing) }.toList()
        items.clear()
        items.addAll(newList)
    }

    override suspend fun loadEventByRequestId(guid: String): List<CSEventData> {
        return items.filter { it.eventRequestGuid == guid } ?: emptyList()
    }

    override suspend fun updateAll(eventDataList: List<CSEventData>) {
        eventDataList.forEach { updatedEvent ->
            val index = items.indexOfFirst { updatedEvent.eventGuid == it.eventGuid }
            if (index != -1) {
                items[index] = updatedEvent
            }
        }
    }

    override suspend fun getUnprocessedEventsWithLimit(limit: Int): List<CSEventData> {
        val unProcessedEvents = items.filter { !it.isOnGoing }
        return if (unProcessedEvents.size > limit) unProcessedEvents.subList(0, limit) else unProcessedEvents
    }

    override suspend fun getAllUnprocessedEvents(): List<CSEventData> {
        return items.filter { !it.isOnGoing }
    }

    override suspend fun getAllUnprocessedEventsCount(): Int {
        return items.filter { !it.isOnGoing }.size
    }

    override suspend fun getEventCount(): Int {
        return items.size
    }
}
