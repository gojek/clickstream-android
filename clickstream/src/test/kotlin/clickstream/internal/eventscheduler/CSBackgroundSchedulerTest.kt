package clickstream.internal.eventscheduler

import clickstream.config.CSEventSchedulerConfig
import clickstream.fake.FakeEventBatchDao
import clickstream.fake.defaultEventWrapperData
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
    private val serviceLocator = mock<CSServiceLocator>()
    private val backgroundLifecycleManager = mock<CSBackgroundLifecycleManager>()
    private val logger = mock<CSLogger>()
    private val guIdGenerator = mock<CSGuIdGenerator>()
    private val timeStampGenerator = mock<CSTimeStampGenerator>()
    private val appLifeCycle = mock<CSAppLifeCycle>()

    private lateinit var scheduler: CSBackgroundScheduler

    @Before
    public fun setup(): Unit = runBlockingTest {
        whenever(networkManager.isAvailable()).thenReturn(true)
        CSServiceLocator.setServiceLocator(serviceLocator)
        scheduler = CSBackgroundScheduler(
            appLifeCycleObserver = appLifeCycle,
            networkManager = networkManager,
            config = CSEventSchedulerConfig.default(),
            batteryStatusObserver = batteryStatusObserver,
            backgroundLifecycleManager = backgroundLifecycleManager,
            dispatcher = dispatcher,
            logger = logger,
            guIdGenerator = guIdGenerator,
            timeStampGenerator = timeStampGenerator,
            eventRepository = eventRepository,
            networkStatusObserver = networkStatusObserver
        )
    }

    @After
    public fun tearDown() {
        dispatcher.cancel()
        verifyNoMoreInteractions(
            networkManager,
            batteryStatusObserver,
            backgroundLifecycleManager,
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
        }

    @Test
    public fun `Given one events exists When scheduler is called Then One event will be forwarded to network layer`(): Unit =
        runBlockingTest {
            eventRepository.insertEventData(CSEventData.create(defaultEventWrapperData()))
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
        }

    @Test
    public fun `Given multiple events exists When scheduler is called Then Multiple event will be sent to network layer`() {
        runBlockingTest {
            eventRepository.insertEventData(CSEventData.create(defaultEventWrapperData()))
            eventRepository.insertEventData(CSEventData.create(defaultEventWrapperData()))

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
        }
    }
}
