package clickstream.internal.eventscheduler

import clickstream.config.CSEventSchedulerConfig
import clickstream.fake.FakeEventBatchDao
import clickstream.fake.defaultEventWrapperData
import clickstream.fake.fakeInfo
import clickstream.internal.analytics.CSHealthEventProcessor
import clickstream.internal.analytics.CSHealthEventRepository
import clickstream.internal.di.CSServiceLocator
import clickstream.internal.eventscheduler.impl.DefaultCSEventRepository
import clickstream.internal.lifecycle.CSAppLifeCycle
import clickstream.internal.lifecycle.CSBackgroundLifecycleManager
import clickstream.internal.networklayer.CSBackgroundNetworkManager
import clickstream.internal.utils.CSBatteryLevel
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSGuIdGenerator
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.internal.utils.CSTimeStampGenerator
import clickstream.logger.CSLogger
import com.gojek.clickstream.internal.Health
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
public class CSBackgroundSchedulerTest {

    private val dispatcher = TestCoroutineDispatcher()
    private val eventRepository = DefaultCSEventRepository(FakeEventBatchDao(dispatcher))

    private val networkManager = mock<CSBackgroundNetworkManager>()
    private val batteryStatusObserver = mock<CSBatteryStatusObserver>()
    private val networkStatusObserver = mock<CSNetworkStatusObserver>()
    private val healthEventProcessor = mock<CSHealthEventProcessor>()
    private val serviceLocator = mock<CSServiceLocator>()
    private val backgroundLifecycleManager = mock<CSBackgroundLifecycleManager>()
    private val healthEventRepository = mock<CSHealthEventRepository>()
    private val logger = mock<CSLogger>()
    private val guIdGenerator = mock<CSGuIdGenerator>()
    private val timeStampGenerator = mock<CSTimeStampGenerator>()
    private val appLifeCycle = mock<CSAppLifeCycle>()

    private lateinit var scheduler: CSBackgroundScheduler

    @Before
    public fun setup(): Unit = runBlockingTest {
        whenever(healthEventRepository.insertHealthEvent(any())).thenReturn(Unit)
        whenever(networkManager.isAvailable()).thenReturn(true)
        whenever(healthEventProcessor.getAggregateEventsBasedOnEventName()).thenReturn(emptyList())
        whenever(serviceLocator.healthEventProcessor).thenReturn(healthEventProcessor)
        CSServiceLocator.setServiceLocator(serviceLocator)
        scheduler = CSBackgroundScheduler(
            appLifeCycleObserver = appLifeCycle,
            networkManager = networkManager,
            config = CSEventSchedulerConfig.default(),
            batteryStatusObserver = batteryStatusObserver,
            backgroundLifecycleManager = backgroundLifecycleManager,
            dispatcher = dispatcher,
            healthEventRepository = healthEventRepository,
            logger = logger,
            guIdGenerator = guIdGenerator,
            timeStampGenerator = timeStampGenerator,
            eventRepository = eventRepository,
            networkStatusObserver = networkStatusObserver,
            info = fakeInfo(),
            eventHealthListener = mock()
        )
    }

    @After
    public fun tearDown() {
        dispatcher.cancel()
        verifyNoMoreInteractions(
            networkManager,
            batteryStatusObserver,
            backgroundLifecycleManager,
            healthEventRepository,
            logger,
            guIdGenerator,
            timeStampGenerator
        )
    }

    @Test
    public fun `Given no event exists When scheduler is called Then No event will be forwarded to network layer`(): Unit =
        runBlockingTest {
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)

            scheduler.sendEvents()

            verify(backgroundLifecycleManager, atLeastOnce()).onStart()
            verify(batteryStatusObserver, never()).getBatteryStatus()
            verify(guIdGenerator, never()).getId()
            verify(timeStampGenerator, never()).getTimeStamp()
            verify(networkManager, atLeastOnce()).isAvailable()
            verify(networkManager, never()).processEvent(any())
            verify(networkManager, atLeastOnce()).isAvailable()
            verify(logger, atLeastOnce()).debug(any())
            verify(healthEventRepository, never()).insertHealthEvent(any())
        }

    @Test
    public fun `Given one events exists When scheduler is called Then One event will be forwarded to network layer`(): Unit =
        runBlockingTest {
            val (eventData, eventHealthData) = CSEventData.create(defaultEventWrapperData())

            eventRepository.insertEventData(eventData)
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)

            scheduler.sendEvents()

            verify(backgroundLifecycleManager, times(1)).onStart()
            verify(batteryStatusObserver, times(1)).getBatteryStatus()
            verify(guIdGenerator, times(1)).getId()
            verify(timeStampGenerator, times(1)).getTimeStamp()
            verify(networkManager, times(1)).processEvent(any())
            verify(networkManager, atLeastOnce()).isAvailable()
            verify(logger, atLeastOnce()).debug(any())
            verify(networkManager, atLeastOnce()).isAvailable()
            verify(healthEventRepository, atLeastOnce()).insertHealthEvent(any())
        }

    @Test
    public fun `Given multiple events exists When scheduler is called Then Multiple event will be sent to network layer`() {
        runBlockingTest {
            val (eventData, eventHealthData) = CSEventData.create(defaultEventWrapperData())

            eventRepository.insertEventData(eventData)
            eventRepository.insertEventData(eventData)

            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())

            scheduler.sendEvents()

            verify(backgroundLifecycleManager, times(1)).onStart()
            verify(batteryStatusObserver, times(1)).getBatteryStatus()
            verify(networkManager, atLeastOnce()).isAvailable()
            verify(networkManager).processEvent(any())
            verify(networkManager, atLeastOnce()).isAvailable()
            verify(guIdGenerator).getId()
            verify(timeStampGenerator).getTimeStamp()
            verify(logger, atLeastOnce()).debug(any())
            verify(healthEventRepository, atLeastOnce()).insertHealthEvent(any())
        }
    }

    @Test
    public fun `Given one health event exists When scheduler is called Then One event will be forwarded to network layer`(): Unit =
        runBlockingTest {
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)
            whenever(healthEventProcessor.getAggregateEventsBasedOnEventName()).thenReturn(
                listOf(
                    Health.getDefaultInstance()
                )
            )
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())

            scheduler.sendEvents()

            verify(backgroundLifecycleManager, times(1)).onStart()
            verify(batteryStatusObserver, times(1)).getBatteryStatus()
            verify(guIdGenerator, times(1)).getId()
            verify(timeStampGenerator, times(1)).getTimeStamp()
            verify(networkManager, times(1)).processEvent(any())
            verify(logger, atLeastOnce()).debug(any())
            verify(networkManager, atLeastOnce()).isAvailable()
            verify(healthEventRepository, never()).insertHealthEvent(any())
        }

    @Test
    public fun `Given one health event and one event exists When scheduler is called Then both will be forwarded to network layer`(): Unit =
        runBlockingTest {
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)

            val (eventData, eventHealthData) = CSEventData.create(defaultEventWrapperData())
            eventRepository.insertEventData(eventData)

            whenever(healthEventProcessor.getAggregateEventsBasedOnEventName()).thenReturn(
                listOf(
                    Health.getDefaultInstance()
                )
            )
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())

            scheduler.sendEvents()

            verify(backgroundLifecycleManager, times(1)).onStart()
            verify(batteryStatusObserver, times(2)).getBatteryStatus()
            verify(guIdGenerator, times(2)).getId()
            verify(timeStampGenerator, times(2)).getTimeStamp()
            verify(networkManager, times(2)).processEvent(any())
            verify(logger, atLeastOnce()).debug(any())
            verify(networkManager, atLeastOnce()).isAvailable()
            verify(healthEventRepository, times(2)).insertHealthEvent(any())
        }

    @Test
    public fun `Given multiple health and app event exists When scheduler is called Then All will be forwarded to network layer`(): Unit =
        runBlockingTest {
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)

            val (eventData, eventHealthData) = CSEventData.create(defaultEventWrapperData())
            eventRepository.insertEventData(eventData)
            eventRepository.insertEventData(eventData)
            whenever(healthEventProcessor.getAggregateEventsBasedOnEventName()).thenReturn(
                listOf(
                    Health.getDefaultInstance(),
                    Health.getDefaultInstance()
                )
            )
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())

            scheduler.sendEvents()

            verify(backgroundLifecycleManager, times(1)).onStart()
            verify(batteryStatusObserver, times(2)).getBatteryStatus()
            verify(guIdGenerator, times(2)).getId()
            verify(timeStampGenerator, times(2)).getTimeStamp()
            verify(networkManager, times(2)).processEvent(any())
            verify(logger, atLeastOnce()).debug(any())
            verify(networkManager, atLeastOnce()).isAvailable()
            verify(healthEventRepository, times(2)).insertHealthEvent(any())
        }
}
