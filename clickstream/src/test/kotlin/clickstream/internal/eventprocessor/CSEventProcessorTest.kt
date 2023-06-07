package clickstream.internal.eventprocessor

import clickstream.CSEvent
import clickstream.config.CSEventProcessorConfig
import clickstream.fake.defaultBytesEventWrapperData
import clickstream.fake.defaultEventWrapperData
import clickstream.fake.fakeInfo
import clickstream.health.constant.CSEventNamesConstant
import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.model.CSHealthEventDTO
import clickstream.internal.eventscheduler.CSEventScheduler
import clickstream.logger.CSLogger
import clickstream.toInternal
import com.gojek.clickstream.products.shuffle.ShuffleCard
import com.google.protobuf.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
public class CSEventProcessorTest {

    private val processorConfig = CSEventProcessorConfig(
        realtimeEvents = listOf("ShuffleCard"),
        instantEvent = listOf("AdCardEvent")
    )
    private val dispatcher = TestCoroutineDispatcher()
    private val eventScheduler = mock<CSEventScheduler>()
    private val logger = mock<CSLogger>()
    private val healthEventRepository = mock<CSHealthEventRepository>()
    private val csInfo = fakeInfo()

    private lateinit var processor: CSEventProcessor

    @Before
    public fun setup() {
        processor = CSEventProcessor(
            config = processorConfig,
            eventScheduler = eventScheduler,
            dispatcher = dispatcher,
            logger = logger,
            healthEventRepository = healthEventRepository,
            info = csInfo,
        )
    }

    @Test
    public fun `it should log correct health events for CSEvents`(): Unit = runBlockingTest {
        val csEventInternal = defaultEventWrapperData("event-uuid").toInternal()

        processor.trackEvent(csEventInternal)

        verify(healthEventRepository).insertHealthEvent(
            CSHealthEventDTO(
                eventName = CSEventNamesConstant.Flushed.ClickStreamEventReceived.value,
                eventType = CSEventTypesConstant.AGGREGATE,
                eventGuid = "event-uuid".plus("_").plus("adcardevent"),
                appVersion = csInfo.appInfo.appVersion
            )
        )
    }

    @Test
    public fun `it should log correct health events for CSBytesEvents`(): Unit = runBlockingTest {
        val csEventInternal = defaultBytesEventWrapperData("event-uuid").toInternal()

        processor.trackEvent(csEventInternal)

        verify(healthEventRepository).insertHealthEvent(
            CSHealthEventDTO(
                eventName = CSEventNamesConstant.Flushed.ClickStreamEventReceived.value,
                eventType = CSEventTypesConstant.AGGREGATE,
                eventGuid = "event-uuid".plus("_").plus("adcardevent"),
                appVersion = csInfo.appInfo.appVersion
            )
        )
    }

    @Test
    public fun `it should forward instant events to schedular`(): Unit = runBlockingTest {
        val csEventInternal = defaultEventWrapperData("event-uuid").toInternal()

        processor.trackEvent(csEventInternal)

        verify(eventScheduler).sendInstantEvent(csEventInternal)
    }

    @Test
    public fun `it should forward non instant events to scheduler`(): Unit = runBlockingTest {
        val shuffleCard = ShuffleCard.getDefaultInstance()
        val csEventInternal =
            CSEvent("event-uuid", Timestamp.getDefaultInstance(), shuffleCard).toInternal()

        processor.trackEvent(csEventInternal)

        verify(eventScheduler).scheduleEvent(csEventInternal)
    }
}