package clickstream.internal.eventscheduler.impl

import clickstream.fake.defaultEventWrapperData
import clickstream.internal.eventscheduler.CSEventData
import clickstream.internal.eventscheduler.CSEventDataDao
import clickstream.internal.eventscheduler.CSEventRepository
import java.util.UUID
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
public class DefaultCSEventRepositoryTest {

    private val dao = mock<CSEventDataDao>()
    private lateinit var repository: CSEventRepository
    private val dbItems = mutableListOf<CSEventData>()

    @Before
    public fun setup() {
        repository = DefaultCSEventRepository(dao)
    }

    @After
    public fun tearDown() {
        verifyNoMoreInteractions(dao)
        dbItems.clear()
    }

    @Test
    public fun `given a data should successfully insert into db`() {
        runBlocking {
            val eventData = CSEventData.create(defaultEventWrapperData())
            whenever(dao.insert(eventData)).then {
                println("adding item")
                dbItems.add(it.getArgument(0))
            }
            whenever(dao.loadAll()).thenReturn(flow { emit(dbItems) })
            dao.insert(eventData)
            dao.loadAll().collect {
                assert(it.size == 1 && it[0] == eventData)
            }
            verify(dao).insert(eventData)
            verify(dao).loadAll()
        }
    }

    @Test
    public fun `given a list of data should successfully insert all into db`() {
        runBlocking {
            val eventData = listOf(CSEventData.create(defaultEventWrapperData()))
            whenever(dao.insertAll(eventData)).then {
                dbItems.addAll(it.getArgument(0))
            }
            whenever(dao.loadAll()).thenReturn(flow { emit(dbItems) })
            dao.insertAll(eventData)
            dao.loadAll().collect {
                assert(it.size == 1 && it == eventData)
            }
            verify(dao).insertAll(eventData)
            verify(dao).loadAll()
        }
    }

    @Test
    public fun `Given an event id should successfully delete from db`() {
        runBlocking {
            val eventData = CSEventData.create(defaultEventWrapperData())
            val eventBatchID = UUID.randomUUID().toString()
            val eventBatch = eventData.copy(eventRequestGuid = eventBatchID)
            dbItems.add(eventBatch)

            whenever(dao.deleteByGuId(eventBatchGuId = eventBatchID)).then { args ->
                dbItems.removeIf { it.eventRequestGuid == args.getArgument(0) }
            }
            whenever(dao.loadAll()).thenReturn(flow { emit(dbItems) })
            dao.deleteByGuId(eventBatchID)
            dao.loadAll().collect {
                assert(it.isEmpty())
            }
            verify(dao).deleteByGuId(eventBatchID)
            verify(dao).loadAll()
        }
    }
}
