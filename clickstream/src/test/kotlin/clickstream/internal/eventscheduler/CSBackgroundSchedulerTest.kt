package clickstream.internal.eventscheduler

import android.app.Application
import clickstream.config.CSRemoteConfig
import clickstream.connection.CSConnectionEvent
import clickstream.connection.CSSocketConnectionListener
import clickstream.extension.messageName
import clickstream.fake.FakeCSHealthEventProcessor
import clickstream.fake.FakeCSHealthEventRepository
import clickstream.fake.FakeCSMetaProvider
import clickstream.fake.createCSConfig
import clickstream.fake.fakeCSAppLifeCycle
import clickstream.fake.fakeCSHealthEventDTOs
import clickstream.fake.fakeCSHealthEventFactory
import clickstream.fake.fakeCSInfo
import clickstream.health.identity.CSGuIdGenerator
import clickstream.health.intermediate.CSEventHealthListener
import clickstream.health.intermediate.CSHealthEventProcessor
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.model.CSEventHealth
import clickstream.health.proto.Health
import clickstream.health.time.CSEventGeneratedTimestampListener
import clickstream.health.time.CSTimeStampGenerator
import clickstream.internal.di.CSServiceLocator
import clickstream.internal.di.impl.DefaultCServiceLocator
import clickstream.internal.networklayer.CSNetworkManager
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.lifecycle.CSBackgroundLifecycleManager
import clickstream.logger.CSLogLevel
import clickstream.logger.CSLogger
import clickstream.proto.User
import clickstream.utils.CoroutineTestRule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
@Ignore
public class CSBackgroundSchedulerTest {

    @get:Rule
    public val coroutineRule: CoroutineTestRule = CoroutineTestRule()

    private val context = mock<Application>()
    private val eventRepository = mock<CSEventRepository>()

    private val fakeCSMetaProvider = FakeCSMetaProvider()
    private val fakeCSHealthEventDTOs = fakeCSHealthEventDTOs(fakeCSMetaProvider)
    private val fakeCSHealthEventRepository = FakeCSHealthEventRepository(fakeCSHealthEventDTOs)
    private val fakeCSHealthEventProcessor = FakeCSHealthEventProcessor(coroutineRule.testDispatcher)
    private val logger = CSLogger(CSLogLevel.OFF)

    private val fakeServiceLocator = DefaultCServiceLocator(
        context = context,
        info = fakeCSInfo,
        config = createCSConfig(),
        eventGeneratedTimestampListener = object : CSEventGeneratedTimestampListener {
            override fun now(): Long {
                return 0L
            }
        },
        socketConnectionListener = object : CSSocketConnectionListener {
            override fun onEventChanged(event: CSConnectionEvent) {
                /*No Op*/
            }
        },
        remoteConfig = object : CSRemoteConfig {
            override val isForegroundEventFlushEnabled: Boolean
                get() = false
        },
        logLevel = CSLogLevel.OFF,
        dispatcher = coroutineRule.testDispatcher,
        eventHealthListener = object : CSEventHealthListener {
            override fun onEventCreated(healthEvent: CSEventHealth) {
                /*No Op*/
            }
        },
        healthEventRepository = fakeCSHealthEventRepository,
        healthEventProcessor = fakeCSHealthEventProcessor,
        healthEventFactory = fakeCSHealthEventFactory,
        appLifeCycle = fakeCSAppLifeCycle,
        eventListener = emptyList()
    )

    private val networkManager = mock<CSNetworkManager>()
    private val batteryStatusObserver = mock<CSBatteryStatusObserver>()
    private val backgroundLifecycleManager = mock<CSBackgroundLifecycleManager>()
    private val guIdGenerator = mock<CSGuIdGenerator>()
    private val timeStampGenerator = mock<CSTimeStampGenerator>()
    private val networkStatusObserver = mock<CSNetworkStatusObserver>()
    private val mockedCSHealthEventRepository = mock<CSHealthEventRepository>()
    private val mockedCSHealthEventProcessor = mock<CSHealthEventProcessor>()

    private lateinit var scheduler: CSWorkManagerEventScheduler

    @Before
    public fun setup(): Unit = runBlockingTest {
        CSServiceLocator.setServiceLocator(fakeServiceLocator)

        scheduler = CSWorkManagerEventScheduler(
            appLifeCycle = fakeCSAppLifeCycle,
            guIdGenerator = guIdGenerator,
            timeStampGenerator = timeStampGenerator,
            batteryStatusObserver = batteryStatusObserver,
            networkStatusObserver = networkStatusObserver,
            eventListeners = emptyList(),
            dispatcher = coroutineRule.testDispatcher,
            healthEventProcessor = mockedCSHealthEventProcessor,
            backgroundLifecycleManager = backgroundLifecycleManager,
            info = fakeCSInfo,
            eventRepository = eventRepository,
            healthEventRepository = mockedCSHealthEventRepository,
            logger = logger,
            networkManager = networkManager
        )
    }

    @Test
    public fun `Given no event exists When scheduler is called Then No event will be forwarded to network layer`() {
        coroutineRule.testDispatcher.runBlockingTest {
            whenever(networkManager.isSocketConnected()).thenReturn(true)
            whenever(eventRepository.getAllEvents()).thenReturn(emptyList())
            whenever(mockedCSHealthEventProcessor.getInstantEvents()).thenReturn(emptyList())
            whenever(mockedCSHealthEventProcessor.getAggregateEvents()).thenReturn(emptyList())

            scheduler.sendEvents()

            verify(networkManager, never()).processEvent(any(), any())
        }
    }

    @Test
    public fun `Given one event exists When scheduler is called Then One event will be forwarded to network layer`() {
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

            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)
            whenever(networkManager.isSocketConnected()).thenReturn(true)
            whenever(eventRepository.getAllEvents()).thenReturn(listOf(event))
            whenever(guIdGenerator.getId()).thenReturn("11")
            whenever(mockedCSHealthEventProcessor.getInstantEvents()).thenReturn(emptyList())
            whenever(mockedCSHealthEventProcessor.getAggregateEvents()).thenReturn(emptyList())

            scheduler.sendEvents()

            verify(networkManager).processEvent(any(), any())
        }
    }

    @Test
    public fun `Given multiple events exists When scheduler is called Then Multiple event will be sent to network layer`() {
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

            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)
            whenever(networkManager.isSocketConnected()).thenReturn(true)
            whenever(eventRepository.getAllEvents()).thenReturn(listOf(event, event))
            whenever(guIdGenerator.getId()).thenReturn("11")
            whenever(mockedCSHealthEventProcessor.getInstantEvents()).thenReturn(emptyList())
            whenever(mockedCSHealthEventProcessor.getAggregateEvents()).thenReturn(emptyList())

            scheduler.sendEvents()

            verify(networkManager).processEvent(any(), any())
        }
    }

    @Test
    public fun `Given one health event exists When scheduler is called Then One event will be forwarded to network layer`() {
        coroutineRule.testDispatcher.runBlockingTest {
            val health = Health.newBuilder().build()

            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)
            whenever(networkManager.isSocketConnected()).thenReturn(true)
            whenever(eventRepository.getAllEvents()).thenReturn(emptyList())
            whenever(guIdGenerator.getId()).thenReturn("11")
            whenever(mockedCSHealthEventProcessor.getInstantEvents()).thenReturn(listOf(health))
            whenever(mockedCSHealthEventProcessor.getAggregateEvents()).thenReturn(listOf(health))

            scheduler.sendEvents()

            verify(networkManager).processEvent(any(), any())
        }
    }

    @Test
    public fun `Given one health event and one event exists When scheduler is called Then both will be forwarded to network layer`() {
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
            val health = Health.newBuilder().build()

            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)
            whenever(networkManager.isSocketConnected()).thenReturn(true)
            whenever(eventRepository.getAllEvents()).thenReturn(listOf(event))
            whenever(guIdGenerator.getId()).thenReturn("11")
            whenever(mockedCSHealthEventProcessor.getInstantEvents()).thenReturn(listOf(health))
            whenever(mockedCSHealthEventProcessor.getAggregateEvents()).thenReturn(listOf(health))

            scheduler.sendEvents()

            verify(networkManager, times(2)).processEvent(any(), any())
        }
    }

    @Test
    public fun `Given multiple health and app event exists When scheduler is called Then All will be forwarded to network layer`() {
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
            val health = Health.newBuilder().build()

            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)
            whenever(networkManager.isSocketConnected()).thenReturn(true)
            whenever(eventRepository.getAllEvents()).thenReturn(listOf(event, event))
            whenever(guIdGenerator.getId()).thenReturn("11")
            whenever(mockedCSHealthEventProcessor.getInstantEvents()).thenReturn(listOf(health, health))
            whenever(mockedCSHealthEventProcessor.getAggregateEvents()).thenReturn(listOf(health, health))

            scheduler.sendEvents()

            verify(networkManager, times(2)).processEvent(any(), any())
        }
    }
}
