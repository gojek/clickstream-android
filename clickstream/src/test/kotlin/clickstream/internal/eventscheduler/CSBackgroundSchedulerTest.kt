package clickstream.internal.eventscheduler

import android.os.Build
import clickstream.config.CSEventSchedulerConfig
import clickstream.config.CSRemoteConfig
import clickstream.fake.FakeEventBatchDao
import clickstream.fake.defaultEventWrapperData
import clickstream.fake.fakeInfo
import clickstream.health.intermediate.CSHealthEventProcessor
import clickstream.health.model.CSEventForHealth
import clickstream.health.model.CSHealthEventConfig
import clickstream.internal.db.CSBatchSizeSharedPref
import clickstream.internal.di.CSServiceLocator
import clickstream.internal.eventscheduler.impl.DefaultCSEventRepository
import clickstream.internal.eventscheduler.impl.NoOpEventSchedulerErrorListener
import clickstream.internal.networklayer.CSNetworkManager
import clickstream.internal.networklayer.socket.CSSocketConnectionManager
import clickstream.internal.utils.CSBatteryLevel
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSGuIdGenerator
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.internal.utils.CSTimeStampGenerator
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.lifecycle.CSBackgroundLifecycleManager
import clickstream.logger.CSLogger
import com.gojek.clickstream.internal.Health
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
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
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
public class CSBackgroundSchedulerTest {

    private val dispatcher = TestCoroutineDispatcher()
    private val eventRepository = DefaultCSEventRepository(FakeEventBatchDao(dispatcher))

    private val networkManager = mock<CSNetworkManager>()
    private val batteryStatusObserver = mock<CSBatteryStatusObserver>()
    private val networkStatusObserver = mock<CSNetworkStatusObserver>()
    private val healthEventProcessor = mock<CSHealthEventProcessor>()
    private val backgroundLifecycleManager = mock<CSBackgroundLifecycleManager>()
    private val logger = mock<CSLogger>()
    private val guIdGenerator = mock<CSGuIdGenerator>()
    private val timeStampGenerator = mock<CSTimeStampGenerator>()
    private val appLifeCycle = mock<CSAppLifeCycle>()
    private val batchSizeRegulator = mock<CSEventBatchSizeStrategy>()
    private val socketConnectionManager = mock<CSSocketConnectionManager>()
    private val remoteConfigMock = mock<CSRemoteConfig>()
    private val batchSizeSharedPrefMock = mock<CSBatchSizeSharedPref>()
    private lateinit var scheduler: CSBackgroundScheduler

    @Before
    public fun setup(): Unit = runBlockingTest {
        whenever(
            healthEventProcessor.insertBatchEvent(any(), any<List<CSEventForHealth>>())
        ).thenReturn(true)
        whenever(networkManager.isSocketAvailable()).thenReturn(true)
        whenever(
            healthEventProcessor.getHealthEventFlow(
                any(),
                any()
            )
        ).thenReturn(flowOf(emptyList()))
        scheduler = CSBackgroundScheduler(
            appLifeCycleObserver = appLifeCycle,
            networkManager = networkManager,
            config = CSEventSchedulerConfig.default(),
            batteryStatusObserver = batteryStatusObserver,
            dispatcher = dispatcher,
            healthProcessor = healthEventProcessor,
            logger = logger,
            guIdGenerator = guIdGenerator,
            timeStampGenerator = timeStampGenerator,
            eventRepository = eventRepository,
            networkStatusObserver = networkStatusObserver,
            info = fakeInfo(),
            eventListeners = emptyList(),
            errorListener = NoOpEventSchedulerErrorListener(),
            csReportDataTracker = null,
            batchSizeRegulator = batchSizeRegulator,
            csSocketConnectionManager = socketConnectionManager,
            remoteConfig = remoteConfigMock,
            batchSizeSharedPref = batchSizeSharedPrefMock,
            csHealthGateway = mock()
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

            verify(backgroundLifecycleManager, never()).onStart()
            verify(batteryStatusObserver, never()).getBatteryStatus()
            verify(guIdGenerator, never()).getId()
            verify(timeStampGenerator, never()).getTimeStamp()
            verify(networkManager, atLeastOnce()).isSocketAvailable()
            verify(networkManager, never()).processEvent(any())
            verify(networkManager, atLeastOnce()).isSocketAvailable()
            verify(logger, atLeastOnce()).debug(any())
            verify(healthEventProcessor, never()).insertBatchEvent(
                any(),
                any<List<CSEventForHealth>>()
            )
        }

    @Test
    public fun `Given one events exists When scheduler is called Then One event will be forwarded to network layer`(): Unit =
        runBlockingTest {
            val eventData = CSEventData.create(defaultEventWrapperData())

            eventRepository.insertEventData(eventData)
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)

            scheduler.sendEvents()

            verify(backgroundLifecycleManager, never()).onStart()
            verify(batteryStatusObserver, times(1)).getBatteryStatus()
            verify(guIdGenerator, times(1)).getId()
            verify(timeStampGenerator, times(1)).getTimeStamp()
            verify(networkManager, times(1)).processEvent(any())
            verify(networkManager, atLeastOnce()).isSocketAvailable()
            verify(logger, atLeastOnce()).debug(any())
            verify(networkManager, atLeastOnce()).isSocketAvailable()
            verify(healthEventProcessor, atLeastOnce()).insertBatchEvent(
                any(),
                any<List<CSEventForHealth>>()
            )
        }

    @Test
    public fun `Given multiple events exists When scheduler is called Then Multiple event will be sent to network layer`() {
        runBlockingTest {
            val eventData = CSEventData.create(defaultEventWrapperData())

            eventRepository.insertEventData(eventData)
            eventRepository.insertEventData(eventData)

            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())

            scheduler.sendEvents()

            verify(backgroundLifecycleManager, never()).onStart()
            verify(batteryStatusObserver, times(1)).getBatteryStatus()
            verify(networkManager, atLeastOnce()).isSocketAvailable()
            verify(networkManager).processEvent(any())
            verify(networkManager, atLeastOnce()).isSocketAvailable()
            verify(guIdGenerator).getId()
            verify(timeStampGenerator).getTimeStamp()
            verify(logger, atLeastOnce()).debug(any())
            verify(healthEventProcessor, atLeastOnce()).insertBatchEvent(
                any(),
                any<List<CSEventForHealth>>()
            )
        }
    }

    @Test
    public fun `Given one health event exists When scheduler is called Then One event will be forwarded to network layer`(): Unit =
        runBlockingTest {
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)
            whenever(healthEventProcessor.getHealthEventFlow(any(), any())).thenReturn(
                flowOf(listOf(Health.getDefaultInstance()))
            )
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())

            scheduler.sendEvents()

            verify(backgroundLifecycleManager, never()).onStart()
            verify(batteryStatusObserver, times(1)).getBatteryStatus()
            verify(guIdGenerator, times(1)).getId()
            verify(timeStampGenerator, times(1)).getTimeStamp()
            verify(networkManager, times(1)).processEvent(any())
            verify(logger, atLeastOnce()).debug(any())
            verify(networkManager, atLeastOnce()).isSocketAvailable()
            verify(healthEventProcessor, never()).insertBatchEvent(
                any(),
                any<List<CSEventForHealth>>()
            )
        }

    @Test
    public fun `Given one health event and one event exists When scheduler is called Then both will be forwarded to network layer`(): Unit =
        runBlockingTest {
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)

            val eventData = CSEventData.create(defaultEventWrapperData())
            eventRepository.insertEventData(eventData)
            whenever(healthEventProcessor.getHealthEventFlow(any(), any())).thenReturn(
                flowOf(listOf(Health.getDefaultInstance()))
            )
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())

            scheduler.sendEvents()

            verify(backgroundLifecycleManager, never()).onStart()
            verify(batteryStatusObserver, times(2)).getBatteryStatus()
            verify(guIdGenerator, times(2)).getId()
            verify(timeStampGenerator, times(2)).getTimeStamp()
            verify(networkManager, times(2)).processEvent(any())
            verify(logger, atLeastOnce()).debug(any())
            verify(networkManager, atLeastOnce()).isSocketAvailable()
            verify(healthEventProcessor, times(1)).insertBatchEvent(
                any(),
                any<List<CSEventForHealth>>()
            )
        }

    @Test
    public fun `Given multiple health and app event exists When scheduler is called Then All will be forwarded to network layer`(): Unit =
        runBlockingTest {
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)

            val eventData = CSEventData.create(defaultEventWrapperData())
            eventRepository.insertEventData(eventData)
            eventRepository.insertEventData(eventData)
            whenever(healthEventProcessor.getHealthEventFlow(any(), any())).thenReturn(
                flowOf((0..3).map { Health.getDefaultInstance() })
            )
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())

            scheduler.sendEvents()

            verify(backgroundLifecycleManager, never()).onStart()
            verify(batteryStatusObserver, times(2)).getBatteryStatus()
            verify(guIdGenerator, times(2)).getId()
            verify(timeStampGenerator, times(2)).getTimeStamp()
            verify(networkManager, times(2)).processEvent(any())
            verify(logger, atLeastOnce()).debug(any())
            verify(networkManager, atLeastOnce()).isSocketAvailable()
            verify(healthEventProcessor, times(1)).insertBatchEvent(
                any(),
                any<List<CSEventForHealth>>()
            )
        }

    @Test
    public fun `Given ignoreBattery flag is true and battery low When sendEvent then it should forward event`(): Unit =
        runBlockingTest(dispatcher) {
            whenever(remoteConfigMock.ignoreBatteryLvlOnFlush).thenReturn(true)
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.LOW_BATTERY)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)
            val eventData = CSEventData.create(defaultEventWrapperData())
            eventRepository.insertEventData(eventData)
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())

            scheduler.sendEvents()

            verify(backgroundLifecycleManager, never()).onStart()
            verify(batteryStatusObserver, never()).getBatteryStatus()
            verify(guIdGenerator, times(1)).getId()
            verify(timeStampGenerator, times(1)).getTimeStamp()
            verify(networkManager, times(1)).processEvent(any())
            verify(logger, atLeastOnce()).debug(any())
            verify(networkManager, atLeastOnce()).isSocketAvailable()
            verify(healthEventProcessor, times(1)).insertBatchEvent(
                any(),
                any<List<CSEventForHealth>>()
            )
        }

    @Test
    public fun `Given ignoreBatteryLvlOnFlush is false and battery adequate When sendEvent then it should forward event`(): Unit =
        runBlockingTest {
            whenever(remoteConfigMock.ignoreBatteryLvlOnFlush).thenReturn(false)
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)

            val eventData = CSEventData.create(defaultEventWrapperData())
            eventRepository.insertEventData(eventData)
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())

            scheduler.sendEvents()

            verify(backgroundLifecycleManager, never()).onStart()
            verify(batteryStatusObserver, atLeastOnce()).getBatteryStatus()
            verify(guIdGenerator, times(1)).getId()
            verify(timeStampGenerator, times(1)).getTimeStamp()
            verify(networkManager, times(1)).processEvent(any())
            verify(logger, atLeastOnce()).debug(any())
            verify(networkManager, atLeastOnce()).isSocketAvailable()
            verify(healthEventProcessor, times(1)).insertBatchEvent(
                any(),
                any<List<CSEventForHealth>>()
            )
        }

    @Test
    public fun `Given multiple events & batching for flush enabled When scheduler is called Then Multiple event will be sent to network layer`() {
        runBlockingTest {
            val eventData1 = CSEventData.create(defaultEventWrapperData())
            val eventData2 = CSEventData.create(defaultEventWrapperData())

            eventRepository.insertEventData(eventData1)
            eventRepository.insertEventData(eventData2)

            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())
            whenever(remoteConfigMock.batchFlushedEvents).thenReturn(true)
            whenever(batchSizeSharedPrefMock.getSavedBatchSize()).thenReturn(1)

            scheduler.sendEvents()

            verify(backgroundLifecycleManager, never()).onStart()
            verify(batteryStatusObserver, times(2)).getBatteryStatus()
            verify(networkManager, atLeastOnce()).isSocketAvailable()
            verify(networkManager, times(2)).processEvent(any())
            verify(guIdGenerator, times(2)).getId()
            verify(timeStampGenerator, times(2)).getTimeStamp()
            verify(logger, atLeastOnce()).debug(any())
            verify(healthEventProcessor, atLeastOnce()).insertBatchEvent(
                any(),
                any<List<CSEventForHealth>>()
            )
        }
    }

    @Test
    public fun `Given health & event & health disabled When scheduler is called Then only event will be forwarded to network layer`(): Unit =
        runBlockingTest {
            whenever(batteryStatusObserver.getBatteryStatus()).thenReturn(CSBatteryLevel.ADEQUATE_POWER)
            whenever(networkStatusObserver.isNetworkAvailable()).thenReturn(true)

            val eventData = CSEventData.create(defaultEventWrapperData())
            eventRepository.insertEventData(eventData)
            whenever(healthEventProcessor.getHealthEventFlow(any(), any())).thenReturn(
                flowOf(emptyList())
            )
            whenever(guIdGenerator.getId()).thenReturn(UUID.randomUUID().toString())
            whenever(timeStampGenerator.getTimeStamp()).thenReturn(System.currentTimeMillis())

            scheduler.sendEvents()

            verify(backgroundLifecycleManager, never()).onStart()
            verify(batteryStatusObserver, times(1)).getBatteryStatus()
            verify(guIdGenerator, times(1)).getId()
            verify(timeStampGenerator, times(1)).getTimeStamp()
            verify(networkManager, times(1)).processEvent(any())
            verify(logger, atLeastOnce()).debug(any())
            verify(networkManager, atLeastOnce()).isSocketAvailable()
            verify(healthEventProcessor, atLeastOnce()).insertBatchEvent(
                any(),
                any<List<CSEventForHealth>>()
            )
        }
}
