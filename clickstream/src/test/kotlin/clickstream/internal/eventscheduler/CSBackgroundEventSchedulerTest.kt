package clickstream.internal.eventscheduler

import clickstream.extension.messageName
import clickstream.fake.FakeCSAppLifeCycle
import clickstream.fake.FakeCSEventListener
import clickstream.fake.FakeCSHealthEventProcessor
import clickstream.fake.fakeCSInfo
import clickstream.health.constant.CSEventNamesConstant
import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.identity.CSGuIdGenerator
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.model.CSHealthEventDTO
import clickstream.health.time.CSTimeStampGenerator
import clickstream.internal.networklayer.CSNetworkManager
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.logger.CSLogLevel
import clickstream.logger.CSLogger
import clickstream.proto.User
import clickstream.utils.CoroutineTestRule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
public class CSBackgroundEventSchedulerTest {

    @get:Rule
    public val coroutineRule: CoroutineTestRule = CoroutineTestRule()

    private val eventRepository = mock<CSEventRepository>()
    private val networkManager = mock<CSNetworkManager>()
    private val batteryStatusObserver = mock<CSBatteryStatusObserver>()
    private val networkStatusObserver = mock<CSNetworkStatusObserver>()
    private val healthEventRepository = mock<CSHealthEventRepository>()
    private val logger = CSLogger(CSLogLevel.OFF)
    private val guIdGenerator = mock<CSGuIdGenerator>()
    private val timeStampGenerator = mock<CSTimeStampGenerator>()
    private val eventListeners = listOf(FakeCSEventListener())
    private val appLifeCycle = FakeCSAppLifeCycle()
    private val healthEventProcessor = FakeCSHealthEventProcessor(coroutineRule.testDispatcher)

    private lateinit var sut: CSBackgroundEventScheduler

    @Before
    public fun setup() {
        sut = CSBackgroundEventScheduler(
            appLifeCycle = appLifeCycle,
            guIdGenerator = guIdGenerator,
            timeStampGenerator = timeStampGenerator,
            batteryStatusObserver = batteryStatusObserver,
            networkStatusObserver = networkStatusObserver,
            eventListeners = eventListeners,
            networkManager = networkManager,
            healthEventProcessor = healthEventProcessor,
            info = fakeCSInfo,
            eventRepository = eventRepository,
            healthEventRepository = healthEventRepository,
            logger = logger,
            dispatcher = coroutineRule.testDispatcher
        )
    }

    @Test
    public fun `verify flushEvents`() {
        coroutineRule.testDispatcher.runBlockingTest {
            val userEvent = User.newBuilder().build()
            val event = CSEventData(
                eventGuid = "1",
                eventRequestGuid = "2",
                eventTimeStamp = 3L,
                isOnGoing = true,
                messageAsBytes = userEvent.toByteArray(),
                messageName = userEvent.messageName()
            )

            val events = listOf(event)
            whenever(eventRepository.getAllEvents())
                .thenReturn(events)
            whenever(guIdGenerator.getId())
                .thenReturn("10")
            whenever(timeStampGenerator.getTimeStamp())
                .thenReturn(1L)

            sut.onStop()

            verify(eventRepository).getAllEvents()

            val flushOnBackgroundDto = CSHealthEventDTO(
                eventName = CSEventNamesConstant.AggregatedAndFlushed.ClickStreamFlushOnBackground.value,
                eventType = CSEventTypesConstant.AGGREGATE,
                eventGuid = events.joinToString { event -> event.eventGuid },
                appVersion = fakeCSInfo.appInfo.appVersion,
                timeToConnection = networkManager.endConnectedTime - networkManager.startConnectingTime
            )
            val batchCreatedDto =  CSHealthEventDTO(
                eventName = CSEventNamesConstant.AggregatedAndFlushed.ClickStreamEventBatchCreated.value,
                eventType = CSEventTypesConstant.AGGREGATE,
                eventBatchGuid = "10",
                eventGuid = event.eventGuid,
                appVersion = fakeCSInfo.appInfo.appVersion,
                timeToConnection = networkManager.endConnectedTime - networkManager.startConnectingTime
            )

            verify(eventRepository, times(2)).insertEventDataList(any())

            verify(healthEventRepository).insertHealthEvent(flushOnBackgroundDto)
            verify(healthEventRepository).insertHealthEvent(batchCreatedDto)

            verifyNoMoreInteractions(healthEventRepository, eventRepository)
        }
    }
}