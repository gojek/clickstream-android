package clickstream.internal.eventscheduler.impl

import clickstream.internal.eventscheduler.CSEventData
import clickstream.internal.eventscheduler.CSEventDataDao
import clickstream.internal.eventscheduler.CSEventRepository
import kotlinx.coroutines.flow.Flow

/**
 * The StorageRepositoryImpl is the implementation detail of the StorageRepository.
 *
 * @param eventDataDao - The Dao object to communicate to the DB
 */
internal class DefaultCSEventRepository(
    private val eventDataDao: CSEventDataDao
) : CSEventRepository {

    override suspend fun insertEventData(eventData: CSEventData): Unit =
        eventDataDao.insert(eventData = eventData)

    override suspend fun insertEventDataList(eventDataList: List<CSEventData>): Unit =
        eventDataDao.insertAll(eventDataList = eventDataList)

    override suspend fun getEventDataList(): Flow<List<CSEventData>> =
        eventDataDao.loadAll()

    override suspend fun getAllEvents(): List<CSEventData> =
        eventDataDao.getAll()

    override suspend fun getOnGoingEvents(): List<CSEventData> =
        eventDataDao.loadOnGoingEvents()

    override suspend fun resetOnGoingForGuid(guid: String): Unit =
        eventDataDao.setOnGoingEvent(guid, false)

    override suspend fun deleteEventDataByGuId(eventBatchGuId: String) {
        eventDataDao.deleteByGuId(eventBatchGuId = eventBatchGuId)
    }
}
