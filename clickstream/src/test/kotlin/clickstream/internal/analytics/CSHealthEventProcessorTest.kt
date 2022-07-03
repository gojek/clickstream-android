package clickstream.internal.analytics

import clickstream.api.CSInfo
import clickstream.fake.FakeCSAppVersionSharedPref
import clickstream.fake.fakeAppInfo
import clickstream.fake.fakeCSHealthEventConfig
import clickstream.fake.fakeCSInfo
import clickstream.fake.fakeUserInfo
import clickstream.health.constant.CSTrackedVia
import clickstream.health.intermediate.CSHealthEventFactory
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.internal.CSHealthEventEntity
import clickstream.health.internal.CSHealthEventEntity.Companion.mapToDtos
import clickstream.health.internal.DefaultCSHealthEventProcessor
import clickstream.internal.analytics.impl.NoOpCSHealthEventLogger
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.logger.CSLogLevel.OFF
import clickstream.logger.CSLogger
import clickstream.utils.CoroutineTestRule
import com.nhaarman.mockitokotlin2.any
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
public class CSHealthEventProcessorTest {

    @get:Rule
    public val coroutineRule: CoroutineTestRule = CoroutineTestRule()

    private val healthEventRepository = mock(CSHealthEventRepository::class.java)
    private val csHealthEventFactory = mock(CSHealthEventFactory::class.java)
    private val csAppVersionSharedPref = FakeCSAppVersionSharedPref(false)
    private val appLifeCycle = mock<CSAppLifeCycle>()

    private lateinit var sut: DefaultCSHealthEventProcessor

    @Test
    public fun `Given CSCustomerInfo and CSMerchantInfo When sendEvents Then verify events are successfully compute`() {
        runBlocking {
            sut = getEventProcessor(
                fakeCSInfo().copy(
                    appInfo = fakeAppInfo.copy(appVersion = "4.38.0")
                )
            )

            val events = fakeCSHealthEvent().mapToDtos()
            whenever(healthEventRepository.getInstantEvents()).thenReturn(events)
            whenever(healthEventRepository.getAggregateEvents()).thenReturn(events)

            sut.onStop()

            verify(healthEventRepository).getInstantEvents()
            verify(healthEventRepository, times(2)).getAggregateEvents()
        }
    }

    @Test
    public fun `Given null CSCustomerInfo When sendEvents Then verify events are successfully compute`() {
        runBlocking {
            sut = getEventProcessor(
                fakeCSInfo().copy(
                    appInfo = fakeAppInfo.copy(appVersion = "4.38.0"),
                    userInfo = fakeUserInfo()
                )
            )

            val events = fakeCSHealthEvent().mapToDtos()
            whenever(healthEventRepository.getInstantEvents()).thenReturn(events)
            whenever(healthEventRepository.getAggregateEvents()).thenReturn(events)

            sut.onStop()

            verify(healthEventRepository).getInstantEvents()
            verify(healthEventRepository, times(2)).getAggregateEvents()
        }
    }

    @Test
    public fun `verify aggregate events`() {
        runBlocking {
            sut = getEventProcessor(
                fakeCSInfo().copy(
                    appInfo = fakeAppInfo.copy(appVersion = "4.38.0"),
                    userInfo = fakeUserInfo()
                )
            )

            whenever(healthEventRepository.getAggregateEvents()).thenReturn(fakeCSHealthEvent().mapToDtos())

            assertTrue(sut.getAggregateEvents().isNotEmpty())
        }
    }

    private fun getEventProcessor(csInfo: CSInfo) = DefaultCSHealthEventProcessor(
        appLifeCycleObserver = appLifeCycle,
        healthEventRepository = healthEventRepository,
        dispatcher = coroutineRule.testDispatcher,
        healthEventConfig = fakeCSHealthEventConfig.copy(trackedVia = CSTrackedVia.Both),
        info = csInfo,
        logger = CSLogger(OFF),
        healthEventLoggerListener = NoOpCSHealthEventLogger(),
        healthEventFactory = csHealthEventFactory,
        appVersion = "4.37.0",
        appVersionPreference = csAppVersionSharedPref
    )

    private fun fakeCSHealthEvent(): List<CSHealthEventEntity> {
        return listOf(
            CSHealthEventEntity(
                healthEventID = 1,
                eventName = "broken-1",
                eventType = "instant",
                timestamp = System.currentTimeMillis().toString(),
                eventId = "1234",
                eventBatchId = "456",
                error = "",
                sessionId = "13455",
                count = 0,
                networkType = "LTE",
                startTime = System.currentTimeMillis(),
                stopTime = System.currentTimeMillis() + 1_000,
                bucketType = "",
                batchSize = 1,
                appVersion = "4.37.0"
            )
        )
    }
}