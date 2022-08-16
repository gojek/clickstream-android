@file:OptIn(ExperimentalCoroutinesApi::class)

package clickstream.eventvisualiser.ui.internal.data.repository

import clickstream.eventvisualiser.ui.internal.data.model.CSEvState
import clickstream.eventvisualiser.ui.internal.data.repository.fakes.FakeEvDatasource
import clickstream.eventvisualiser.ui.internal.data.repository.fakes.FakeEvEventObserver
import clickstream.eventvisualiser.ui.internal.data.repository.fakes.FakeEvEventRepository
import clickstream.listener.CSEventModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class CSEvRepositoryImplTest {

    private val csObserver = FakeEvEventObserver()
    private val csEvDataSource = FakeEvDatasource(csObserver)
    private val csEvRepository = FakeEvEventRepository(csEvDataSource)


    @Before
    fun setUp() {
        csEvRepository.startObserving()
    }

    @After
    fun tearDown() = runBlocking {
        csEvRepository.stopObserving()
        csEvRepository.clearData()
    }

    @Test
    fun `Given events received from observer check if getAllEventNames returns correct events`() =
        runBlocking {
            (1..500).forEach {
                emitScheduledEvent(it.toString(), it.toString())
            }
            delay(100)
            val events = csEvRepository.getAllEventNames()
            assert(events.size == 500)
        }

    @Test
    fun `Given events received from observer and key, value filters check if getAllEventNames returns correct events`() =
        runBlocking {
            (1..500).forEach {
                emitScheduledEvent(it.toString(), it.toString())
            }

            (501..1000).forEach {
                emitScheduledEvent(
                    it.toString(), it.toString(), properties = mapOf(
                        "address.firstLine" to "[]",
                        "eventMeta.sae" to "uui"
                    )
                )
            }

            delay(100)
            var events = csEvRepository.getAllEventNames(keys = listOf("No events"))
            assert(events.isEmpty())
            events = csEvRepository.getAllEventNames(keys = listOf("name"))
            assert(events.size == 500)
            events = csEvRepository.getAllEventNames(values = listOf("9953319602"))
            assert(events.size == 500)
            events = csEvRepository.getAllEventNames(values = listOf("uui"))
            events = csEvRepository.getAllEventNames(
                keys = listOf("firs", "sae"),
                values = listOf("uui")
            )
            assert(events.size == 500)
        }

    @Test
    fun `Given scheduled events and new acknowledged from observer check if getEventDetailList returns correct events`() =
        runBlocking {
            (1..2000).forEach {
                emitScheduledEvent("Event", it.toString())
            }
            (1..100).forEach {
                emitAckEvents("Event", it.toString())
            }

            delay(100)
            csEvRepository.getEventDetailList("Events")
                .filter { it.eventId.toInt() in (1..50) }
                .forEach {
                    assert(it.state == CSEvState.ACKNOWLEDGED)
                }
        }

    @Test
    fun `Given events in data source check if getFilteredList returns correct events`() =
        runBlocking {
            (1..100).forEach {
                val evenName = when (it) {
                    in (1..20) -> "Page events"
                    in (21..40) -> "Pub sub events"
                    in (41..60) -> "Phone events"
                    else -> "Carlos Events"
                }
                emitScheduledEvent(evenName, it.toString())
            }
            delay(100)
            assert(csEvRepository.getAllEventNames().size == 4)
            assert(csEvRepository.getFilteredList("sub").size == 1)
            assert(csEvRepository.getFilteredList("Sub").size == 1)
            assert(csEvRepository.getFilteredList("p").size == 3)
            assert(csEvRepository.getFilteredList("pue").isEmpty())
            assert(csEvRepository.getFilteredList("eve      ").size == 4)
        }

    @Test
    fun `Given events in data source check if getEventProperties returns correct events`() =
        runBlocking {
            emitScheduledEvent(
                eventName = "Event",
                eventId = "1",
                properties = mapOf("a" to "1", "b" to "2")
            )
            emitScheduledEvent(
                eventName = "Event2",
                eventId = "2",
                properties = mapOf("c" to "3", "d" to "4")
            )
            emitAckEvents(
                eventName = "Event",
                eventId = "1",
            )
            delay(100)
            assert(csEvRepository.getEventProperties("Event", "1").containsKey("a"))
            assert(csEvRepository.getEventProperties("Random event", "1").isEmpty())
            assert(csEvRepository.getEventProperties("Event2", "2").size == 2)
        }

    private fun emitScheduledEvent(
        eventName: String,
        eventId: String,
        productName: String = "Product$eventId",
        properties: Map<String, Any?> = mapOf(
            "name" to "kshitij",
            "phone" to "9953319602"
        )
    ) {
        csObserver.onEventChanged(
            listOf(
                CSEventModel.Event.Scheduled(
                    eventName = eventName,
                    eventId = eventId,
                    productName = productName,
                    properties = properties,
                    timeStamp = System.currentTimeMillis(),
                )
            )
        )
    }

    private fun emitAckEvents(eventName: String, eventId: String) {
        csObserver.onEventChanged(
            listOf(
                CSEventModel.Event.Acknowledged(
                    eventName = eventName,
                    eventId = eventId,
                    productName = "Product$eventId",
                    timeStamp = System.currentTimeMillis()
                )
            )
        )
    }
}