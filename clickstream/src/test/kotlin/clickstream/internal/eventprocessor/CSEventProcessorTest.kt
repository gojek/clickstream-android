package clickstream.internal.eventprocessor

import clickstream.CSEvent
import clickstream.config.CSEventProcessorConfig
import clickstream.fake.defaultEventWrapperData
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

    private lateinit var processor: CSEventProcessor

    @Before
    public fun setup() {
        processor = CSEventProcessor(
            config = processorConfig,
            eventScheduler = eventScheduler,
            dispatcher = dispatcher,
            logger = logger,
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