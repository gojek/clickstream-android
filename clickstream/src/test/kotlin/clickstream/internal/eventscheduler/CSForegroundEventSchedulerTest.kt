package clickstream.internal.eventscheduler

import clickstream.config.CSEventSchedulerConfig
import clickstream.extension.messageName
import clickstream.fake.FakeCSAppLifeCycle
import clickstream.fake.FakeCSEventListener
import clickstream.fake.fakeInfo
import clickstream.health.constant.CSEventNamesConstant
import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.identity.CSGuIdGenerator
import clickstream.health.intermediate.CSEventHealthListener
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.model.CSHealthEventDTO
import clickstream.health.time.CSTimeStampGenerator
import clickstream.internal.networklayer.CSNetworkManager
import clickstream.internal.utils.CSBatteryLevel
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.internal.utils.CSResult
import clickstream.logger.CSLogLevel
import clickstream.logger.CSLogger
import clickstream.model.CSEvent
import clickstream.utils.CoroutineTestRule
import com.gojek.clickstream.products.events.AdCardEvent
import com.google.protobuf.Timestamp
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
public class CSForegroundEventSchedulerTest {

    @get:Rule
    public val coroutineRule: CoroutineTestRule = CoroutineTestRule()

    private val config = CSEventSchedulerConfig(
        eventsPerBatch = 2,
        batchPeriod = 2000,
        flushOnBackground = false,
        connectionTerminationTimerWaitTimeInMillis = 5,
        backgroundTaskEnabled = false,
        workRequestDelayInHr = 1,
        utf8ValidatorEnabled = true
    )

    private val eventRepository = mock<CSEventRepository>()
    private val networkManager = mock<CSNetworkManager>()
    private val batteryStatusObserver = mock<CSBatteryStatusObserver>()
    private val networkStatusObserver = mock<CSNetworkStatusObserver>()
    private val healthEventRepository = mock<CSHealthEventRepository>()
    private val logger = CSLogger(CSLogLevel.OFF)
    private val guIdGenerator = mock<CSGuIdGenerator>()
    private val timeStampGenerator = mock<CSTimeStampGenerator>()
    private val eventHealthListener = mock<CSEventHealthListener>()
    private val eventListeners = listOf(FakeCSEventListener())
    private val appLifeCycle = FakeCSAppLifeCycle()

    private lateinit var sut: CSForegroundEventScheduler

    @Before
    public fun setup() {
        sut = CSForegroundEventScheduler(
            appLifeCycle = appLifeCycle,
            networkManager = networkManager,
            eventRepository = eventRepository,
            timeStampGenerator = timeStampGenerator,
            guIdGenerator = guIdGenerator,
            logger = logger,
            healthEventRepository = healthEventRepository,
            dispatcher = coroutineRule.testDispatcher,
            batteryStatusObserver = batteryStatusObserver,
            networkStatusObserver = networkStatusObserver,
            config = config,
            info = fakeInfo(),
            eventHealthListener = eventHealthListener,
            eventListeners = eventListeners
        )
    }

    @Test
    public fun `Given an AdCard Event When app state is active Then verify event should went to network manager`() {
        coroutineRule.testDispatcher.runBlockingTest {
            val adCardEvent = AdCardEvent.newBuilder().build()
            val event = CSEventData(
                eventGuid = "1",
                eventRequestGuid = "2",
                eventTimeStamp = 3L,
                isOnGoing = true,
                messageAsBytes = adCardEvent.toByteArray(),
                messageName = adCardEvent.messageName()
            )

            whenever(eventRepository.getEventDataList())
                .thenReturn(flowOf(listOf(event)))
            whenever(eventRepository.getEventsOnGuId(any()))
                .thenReturn(listOf(event))
            whenever(eventRepository.getOnGoingEvents())
                .thenReturn(listOf(event))
            whenever(networkManager.eventGuidFlow)
                .thenReturn(flowOf(CSResult.Success("2")))
            whenever(networkStatusObserver.isNetworkAvailable())
                .thenReturn(true)
            whenever(batteryStatusObserver.getBatteryStatus())
                .thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkManager.isSocketConnected())
                .thenReturn(true)
            whenever(guIdGenerator.getId())
                .thenReturn("92")
            whenever(timeStampGenerator.getTimeStamp())
                .thenReturn(922)

            sut.onStart()

            // simulate initial delay
            coroutineRule.testDispatcher.advanceTimeBy(10)
            sut.cancelJob()

            verify(networkManager).processEvent(any(), any())
            verify(networkManager).eventGuidFlow
            verify(networkManager, times(2)).isSocketConnected()

            verify(eventRepository, times(2)).insertEventDataList(any())
            verify(eventRepository).getEventDataList()
            verify(eventRepository).getOnGoingEvents()
            verify(eventRepository).deleteEventDataByGuId("2")
            verify(eventRepository).getEventsOnGuId(any())

            verifyNoMoreInteractions(eventRepository)
        }
    }

    @Test
    public fun `Given No Event When app state is active Then verify no event get proceses to network manager`() {
        coroutineRule.testDispatcher.runBlockingTest {
            whenever(eventRepository.getEventDataList())
                .thenReturn(flowOf(emptyList()))
            whenever(eventRepository.getOnGoingEvents())
                .thenReturn(emptyList())
            whenever(networkManager.eventGuidFlow)
                .thenReturn(emptyFlow())

            sut.onStart()

            // simulate initial delay
            coroutineRule.testDispatcher.advanceTimeBy(10)
            sut.cancelJob()

            verify(networkManager, never()).processEvent(any(), any())
            verify(networkManager).eventGuidFlow

            verify(eventRepository, never()).insertEventDataList(any())
            verify(eventRepository).getEventDataList()
            verify(eventRepository).getOnGoingEvents()
            verify(eventRepository, never()).deleteEventDataByGuId("2")

            verifyNoMoreInteractions(eventRepository)
        }
    }

    @Test
    public fun `verify scheduleEvent`() {
        coroutineRule.testDispatcher.runBlockingTest {
            val adCardEvent = AdCardEvent.newBuilder().build()
            val event = CSEvent(
                guid = "1",
                timestamp = Timestamp.getDefaultInstance(),
                message = adCardEvent
            )

            sut.scheduleEvent(event)

            val cacheDto = CSHealthEventDTO(
                eventName = CSEventNamesConstant.AggregatedAndFlushed.ClickStreamEventCached.value,
                eventType = CSEventTypesConstant.AGGREGATE,
                eventGuid = event.guid,
                appVersion = fakeInfo().appInfo.appVersion
            )
            verify(eventHealthListener).onEventCreated(any())
            verify(eventRepository).insertEventData(any())
            verify(healthEventRepository).insertHealthEvent(cacheDto)

            eventListeners.forEach {
                assertTrue(it.isCalled)
            }
        }
    }

    @Test
    public fun `verify sendInstantEvent`() {
        coroutineRule.testDispatcher.runBlockingTest {
            val adCardEvent = AdCardEvent.newBuilder().build()
            val event = CSEvent(
                guid = "1",
                timestamp = Timestamp.getDefaultInstance(),
                message = adCardEvent
            )

            whenever(timeStampGenerator.getTimeStamp())
                .thenReturn(1L)
            whenever(guIdGenerator.getId())
                .thenReturn("1")

            sut.sendInstantEvent(event)

            verify(eventHealthListener).onEventCreated(any())
            verify(networkManager).processInstantEvent(any())

            eventListeners.forEach {
                assertTrue(it.isCalled)
            }
        }
    }
}
