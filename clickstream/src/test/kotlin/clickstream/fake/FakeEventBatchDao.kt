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

    override fun getAll(): List<CSEventData> {
        return items
    }

    override fun loadOnGoingEvents(): List<CSEventData> {
        return items.filter { it.isOnGoing }
    }

    override fun insert(eventData: CSEventData) {
        items.add(eventData)
    }

    override fun insertAll(eventDataList: List<CSEventData>) {
        items.addAll(eventDataList)
    }

    override fun deleteByGuId(eventBatchGuId: String) {
        items.removeIf { it.eventRequestGuid == eventBatchGuId }
    }

    override fun setOnGoingEvent(guid: String, ongoing: Boolean) {
        val newList = items.map { it.copy(isOnGoing = ongoing) }.toList()
        items.clear()
        items.addAll(newList)
    }
}
