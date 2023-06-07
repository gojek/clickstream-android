package clickstream.internal.eventscheduler

import android.os.Build
import clickstream.config.CSEventSchedulerConfig
import clickstream.config.CSRemoteConfig
import clickstream.fake.FakeEventBatchDao
import clickstream.fake.defaultBytesEventWrapperData
import clickstream.fake.defaultEventWrapperData
import clickstream.fake.fakeInfo
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.internal.eventscheduler.impl.DefaultCSEventRepository
import clickstream.internal.eventscheduler.impl.NoOpEventSchedulerErrorListener
import clickstream.internal.lifecycle.CSAppLifeCycle
import clickstream.internal.networklayer.CSNetworkManager
import clickstream.internal.utils.CSBatteryLevel
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSGuIdGenerator
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.internal.utils.CSTimeStampGenerator
import clickstream.logger.CSLogger
import clickstream.toInternal
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import clickstream.internal.lifecycle.CSSocketConnectionManager

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
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
        utf8ValidatorEnabled = true,
        enableForegroundFlushing = false
    )

    private val networkManager = mock<CSNetworkManager>()
    private val batteryStatusObserver = mock<CSBatteryStatusObserver>()
    private val networkStatusObserver = mock<CSNetworkStatusObserver>()
    private val healthEventRepository = mock<CSHealthEventRepository>()
    private val logger = mock<CSLogger>()
    private val guIdGenerator = mock<CSGuIdGenerator>()
    private val timeStampGenerator = mock<CSTimeStampGenerator>()
    private val appLifeCycle = mock<CSAppLifeCycle>()
    private val batchSizeRegulator = mock<CSEventBatchSizeStrategy>()
    private val connectionManger = mock<CSSocketConnectionManager>()
    private val remoteConfig = mock<CSRemoteConfig>()

    private lateinit var scheduler: CSEventScheduler

    @Before
    public fun setup() {
        scheduler = CSEventScheduler(
            appLifeCycleObserver = appLifeCycle,
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
            eventHealthListener = mock(),
            eventListeners = emptyList(),
            errorListener = NoOpEventSchedulerErrorListener(),
            csReportDataTracker = null,
            batchSizeRegulator = batchSizeRegulator,
            socketConnectionManager = connectionManger,
            remoteConfig = remoteConfig
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
            whenever(networkManager.isSocketAvailable()).thenReturn(true)
            whenever(batchSizeRegulator.regulatedCountOfEventsPerBatch(any())).thenReturn(1)

            scheduler.scheduleEvent(defaultEventWrapperData().toInternal())
            dispatcher.advanceTimeBy(2000)

            verify(batchSizeRegulator, times(1)).logEvent(any())
            verify(networkManager, times(1)).eventGuidFlow
            verify(networkManager, atLeastOnce()).isSocketAvailable()
            verify(networkManager, times(1)).processEvent(any())
            verify(guIdGenerator, times(1)).getId()
            verify(timeStampGenerator, times(1)).getTimeStamp()
            verify(batchSizeRegulator, atLeastOnce()).regulatedCountOfEventsPerBatch(any())
        }

    @Test
    public fun `Given the scheduler & bytes event should dispatch event at given interval & verify the results`(): Unit =
        runBlockingTest {
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)
            whenever(networkManager.isSocketAvailable()).thenReturn(true)
            whenever(batchSizeRegulator.regulatedCountOfEventsPerBatch(any())).thenReturn(1)

            scheduler.scheduleEvent(defaultBytesEventWrapperData().toInternal())
            dispatcher.advanceTimeBy(2000)

            verify(batchSizeRegulator, times(1)).logEvent(any())
            verify(networkManager, times(1)).eventGuidFlow
            verify(networkManager, atLeastOnce()).isSocketAvailable()
            verify(networkManager, times(1)).processEvent(any())
            verify(guIdGenerator, times(1)).getId()
            verify(timeStampGenerator, times(1)).getTimeStamp()
            verify(batchSizeRegulator, atLeastOnce()).regulatedCountOfEventsPerBatch(any())
        }

    @Test
    public fun `Given the scheduler & event list should dispatch event at given interval & verify the results`() {
        runBlockingTest {
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)
            whenever(networkManager.isSocketAvailable()).thenReturn(true)
            whenever(batchSizeRegulator.regulatedCountOfEventsPerBatch(any())).thenReturn(1)
            repeat(6) {
                scheduler.scheduleEvent(defaultEventWrapperData().toInternal())
            }

            dispatcher.advanceTimeBy(6000)

            verify(batchSizeRegulator, times(6)).logEvent(any())
            verify(networkManager, times(1)).eventGuidFlow
            verify(networkManager, times(3)).processEvent(any())
            verify(networkManager, atLeastOnce()).isSocketAvailable()
            verify(guIdGenerator, times(3)).getId()
            verify(timeStampGenerator, times(3)).getTimeStamp()
            verify(batchSizeRegulator, atLeastOnce()).regulatedCountOfEventsPerBatch(any())
        }
    }

    @Test
    public fun `Given the scheduler & bytes event list should dispatch event at given interval & verify the results`() {
        runBlockingTest {
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)
            whenever(networkManager.isSocketAvailable()).thenReturn(true)
            whenever(batchSizeRegulator.regulatedCountOfEventsPerBatch(any())).thenReturn(2)
            repeat(6) {
                scheduler.scheduleEvent(defaultBytesEventWrapperData().toInternal())
            }

            dispatcher.advanceTimeBy(6000)

            verify(batchSizeRegulator, times(6)).logEvent(any())
            verify(networkManager, times(1)).eventGuidFlow
            verify(networkManager, times(3)).processEvent(any())
            verify(networkManager, atLeastOnce()).isSocketAvailable()
            verify(guIdGenerator, times(3)).getId()
            verify(timeStampGenerator, times(3)).getTimeStamp()
            verify(batchSizeRegulator, atLeastOnce()).regulatedCountOfEventsPerBatch(any())
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
            verify(batchSizeRegulator, times(1)).regulatedCountOfEventsPerBatch(any())
        }
    }

    @Test
    public fun `Given ignoreBatteryLvlCheck is true and not Flushing then it should dispatch event at given interval & verify the results`(): Unit =
        runBlockingTest {
            whenever(remoteConfig.ignoreBatteryLvlOnFlush).thenReturn(true)
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)
            whenever(networkManager.isSocketAvailable()).thenReturn(true)
            whenever(batchSizeRegulator.regulatedCountOfEventsPerBatch(any())).thenReturn(1)

            scheduler.scheduleEvent(defaultEventWrapperData().toInternal())
            dispatcher.advanceTimeBy(2000)

            verify(batteryStatusObserver, atLeastOnce()).getBatteryStatus()
            verify(batchSizeRegulator, times(1)).logEvent(any())
            verify(networkManager, times(1)).eventGuidFlow
            verify(networkManager, atLeastOnce()).isSocketAvailable()
            verify(networkManager, times(1)).processEvent(any())
            verify(guIdGenerator, times(1)).getId()
            verify(timeStampGenerator, times(1)).getTimeStamp()
            verify(batchSizeRegulator, atLeastOnce()).regulatedCountOfEventsPerBatch(any())
        }

    @Test
    public fun `Given ignoreBatteryLvlCheck is false and not Flushing then it should dispatch event at given interval & verify the results`(): Unit =
        runBlockingTest {
            whenever(remoteConfig.ignoreBatteryLvlOnFlush).thenReturn(false)
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)
            whenever(networkManager.isSocketAvailable()).thenReturn(true)
            whenever(batchSizeRegulator.regulatedCountOfEventsPerBatch(any())).thenReturn(1)

            scheduler.scheduleEvent(defaultEventWrapperData().toInternal())
            dispatcher.advanceTimeBy(2000)

            verify(batteryStatusObserver, atLeastOnce()).getBatteryStatus()
            verify(batchSizeRegulator, times(1)).logEvent(any())
            verify(networkManager, times(1)).eventGuidFlow
            verify(networkManager, atLeastOnce()).isSocketAvailable()
            verify(networkManager, times(1)).processEvent(any())
            verify(guIdGenerator, times(1)).getId()
            verify(timeStampGenerator, times(1)).getTimeStamp()
            verify(batchSizeRegulator, atLeastOnce()).regulatedCountOfEventsPerBatch(any())
        }

    @Test
    public fun `Given ignoreBatteryLvlCheck is false and not Flushing and low battery then it shouldn't invoke network manager`(): Unit =
        runBlockingTest {
            whenever(remoteConfig.ignoreBatteryLvlOnFlush).thenReturn(false)
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.LOW_BATTERY)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)
            whenever(networkManager.isSocketAvailable()).thenReturn(true)
            whenever(batchSizeRegulator.regulatedCountOfEventsPerBatch(any())).thenReturn(1)

            scheduler.scheduleEvent(defaultEventWrapperData().toInternal())
            dispatcher.advanceTimeBy(2000)
            dispatcher.advanceTimeBy(2000)

            verify(networkManager, times(1)).eventGuidFlow
            verify(networkManager, never()).processEvent(any())
            verifyZeroInteractions(networkManager)
            verifyZeroInteractions(guIdGenerator)
            verifyZeroInteractions(timeStampGenerator)
            verify(batchSizeRegulator, atLeastOnce()).regulatedCountOfEventsPerBatch(any())
        }
}
