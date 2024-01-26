package clickstream.internal.eventscheduler.impl

import clickstream.CSBytesEvent
import clickstream.internal.db.CSBatchSizeSharedPref
import clickstream.internal.eventscheduler.DEFAULT_MIN_EVENT_COUNT
import clickstream.logger.CSLogger
import clickstream.toInternal
import com.google.protobuf.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
public class EventByteSizeBasedBatchStrategyTest {
    private val logger = mock<CSLogger>()
    private val csBatchSizeSharedPref = mock<CSBatchSizeSharedPref>()
    private lateinit var sut: EventByteSizeBasedBatchStrategy

    @Before
    public fun setup() {
        sut = EventByteSizeBasedBatchStrategy(logger, csBatchSizeSharedPref)
    }

    @Test
    public fun `it should return default minimum if no events are yet observed`(): Unit = runBlockingTest {
        val countOfEventsPerBatch = sut.regulatedCountOfEventsPerBatch(4)
        assertEquals(DEFAULT_MIN_EVENT_COUNT, countOfEventsPerBatch)
    }

    @Test
    public fun `it should return default minimum if expectedBatchSize is bigger than average calculated size`(): Unit = runBlockingTest {
        val csEvent = CSBytesEvent("guid", Timestamp.getDefaultInstance(), "name", ByteArray(25000))

        sut.logEvent(csEvent.toInternal())

        val countOfEventsPerBatch = sut.regulatedCountOfEventsPerBatch(20000)
        assertEquals(DEFAULT_MIN_EVENT_COUNT, countOfEventsPerBatch)
    }
}