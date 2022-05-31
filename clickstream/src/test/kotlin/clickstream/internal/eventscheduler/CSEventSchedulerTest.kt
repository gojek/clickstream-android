package clickstream.internal.eventscheduler

import clickstream.config.CSEventSchedulerConfig
import clickstream.fake.FakeEventBatchDao
import clickstream.fake.defaultEventWrapperData
import clickstream.fake.fakeInfo
import clickstream.health.CSTimeStampGenerator
import clickstream.health.identity.CSGuIdGenerator
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.internal.eventscheduler.impl.DefaultCSEventRepository
import clickstream.internal.networklayer.CSNetworkManager
import clickstream.internal.utils.CSBatteryLevel
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.lifecycle.CSAppLifeCycle
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
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
public class CSEventSchedulerTest {

    private val dispatcher = TestCoroutineDispatcher()
    private val eventRepository = DefaultCSEventRepository(FakeEventBatchDao(dispatcher))
    private val config = CSEventSchedulerConfig(
        eventsPerBatch = 2,
        batchPeriod = 2000,
        flushOnBackground = false,
        connectionTerminationTimerWaitTimeInMillis = 5,
        backgroundTaskEnabled = false,
        workRequestDelayInHr = 1,
        utf8ValidatorEnabled = true
    )

    private val networkManager = mock<CSNetworkManager>()
    private val batteryStatusObserver = mock<CSBatteryStatusObserver>()
    private val networkStatusObserver = mock<CSNetworkStatusObserver>()
    private val healthEventRepository = mock<CSHealthEventRepository>()
    private val logger = mock<CSLogger>()
    private val guIdGenerator = mock<CSGuIdGenerator>()
    private val timeStampGenerator = mock<CSTimeStampGenerator>()
    private val appLifeCycle = mock<CSAppLifeCycle>()

    private lateinit var scheduler: CSEventScheduler

    @Before
    public fun setup() {
        scheduler = CSEventScheduler(
            appLifeCycle = appLifeCycle,
            networkManager = networkManager,
            eventRepository = eventRepository,
            timeStampGenerator = timeStampGenerator,
            guIdGenerator = guIdGenerator,
            logger = logger,
            healthEventRepository = healthEventRepository,
            dispatcher = dispatcher,
            batteryStatusObserver = batteryStatusObserver,
            networkStatusObserver = networkStatusObserver,
            config = config,
            info = fakeInfo(),
            eventHealthListener = mock()
        )
        scheduler.onStart()
    }

    @After
    public fun tearDown() {
        scheduler.onStop()
        dispatcher.cancel()
        verifyNoMoreInteractions(
            networkManager,
            guIdGenerator,
            timeStampGenerator
        )
    }

    @Test
    public fun `Given the scheduler & event should dispatch event at given interval & verify the results`(): Unit =
        runBlockingTest {
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)
            whenever(networkManager.isAvailable()).thenReturn(true)

            scheduler.scheduleEvent(defaultEventWrapperData())
            dispatcher.advanceTimeBy(2000)

            verify(networkManager, times(1)).eventGuidFlow
            verify(networkManager, atLeastOnce()).isAvailable()
            verify(networkManager, times(1)).processEvent(any())
            verify(logger, atLeastOnce()).debug(any())
            verify(guIdGenerator, times(1)).getId()
            verify(timeStampGenerator, times(1)).getTimeStamp()
        }

    @Test
    public fun `Given the scheduler & event list should dispatch event at given interval & verify the results`() {
        runBlockingTest {
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)
            whenever(networkManager.isAvailable()).thenReturn(true)
            repeat(6) {
                scheduler.scheduleEvent(defaultEventWrapperData())
            }

            dispatcher.advanceTimeBy(6000)

            verify(logger, atLeastOnce()).debug(any())
            verify(networkManager, times(1)).eventGuidFlow
            verify(networkManager, times(3)).processEvent(any())
            verify(networkManager, atLeastOnce()).isAvailable()
            verify(guIdGenerator, times(3)).getId()
            verify(timeStampGenerator, times(3)).getTimeStamp()
        }
    }

    @Test
    public fun `Given the scheduler should dispatch event when list is empty & shouldn't invoke network manager`() {
        runBlockingTest {
            dispatcher.advanceTimeBy(2000)

            verify(networkManager, times(1)).eventGuidFlow
            verify(networkManager, never()).processEvent(any())
            verifyZeroInteractions(networkManager)
            verifyZeroInteractions(guIdGenerator)
            verifyZeroInteractions(timeStampGenerator)
        }
    }
}
