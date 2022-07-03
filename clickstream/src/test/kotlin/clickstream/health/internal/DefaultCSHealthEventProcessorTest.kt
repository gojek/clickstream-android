package clickstream.health.internal

import clickstream.api.CSAppInfo
import clickstream.fake.FakeCSAppLifeCycle
import clickstream.fake.FakeCSAppVersionSharedPref
import clickstream.fake.FakeCSHealthEventLoggerListener
import clickstream.fake.FakeCSHealthEventRepository
import clickstream.fake.FakeCSMetaProvider
import clickstream.fake.fakeCSHealthEventConfig
import clickstream.fake.fakeCSHealthEventDTOs
import clickstream.fake.fakeCSInfo
import clickstream.health.constant.CSEventNamesConstant
import clickstream.health.identity.DefaultCSGuIdGenerator
import clickstream.health.time.CSTimeStampGenerator
import clickstream.logger.CSLogLevel
import clickstream.logger.CSLogger
import clickstream.utils.CoroutineTestRule
import java.util.regex.Pattern
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
public class DefaultCSHealthEventProcessorTest {

    @get:Rule
    public val coroutineRule: CoroutineTestRule = CoroutineTestRule()

    private val fakeCSMetaProvider = FakeCSMetaProvider()
    private val fakeCSHealthEventDTOs = fakeCSHealthEventDTOs(fakeCSMetaProvider)
    private val fakeCSInfo = fakeCSInfo(fakeCSMetaProvider)
    private val fakeCSAppLifeCycle = FakeCSAppLifeCycle()
    private val fakeCSHealthEventRepository = FakeCSHealthEventRepository(fakeCSHealthEventDTOs)
    private val fakeCSHealthEventLoggerListener = FakeCSHealthEventLoggerListener()
    private val fakeCSHealthEventFactory = DefaultCSHealthEventFactory(
        guIdGenerator = DefaultCSGuIdGenerator(),
        timeStampGenerator = object : CSTimeStampGenerator {
            override fun getTimeStamp(): Long {
                return 1
            }
        },
        metaProvider = fakeCSMetaProvider
    )
    private val uuidRegex = Pattern.compile("[a-f0-9]{8}(?:-[a-f0-9]{4}){4}[a-f0-9]{8}")
    private val fakeCSAppVersionSharedPref = FakeCSAppVersionSharedPref(true)
    private lateinit var sut: DefaultCSHealthEventProcessor

    @Before
    public fun setup() {
        sut = DefaultCSHealthEventProcessor(
            appLifeCycleObserver = fakeCSAppLifeCycle,
            healthEventRepository = fakeCSHealthEventRepository,
            dispatcher = coroutineRule.testDispatcher,
            healthEventConfig = fakeCSHealthEventConfig,
            info = fakeCSInfo.copy(appInfo = CSAppInfo(fakeCSMetaProvider.app.version)),
            logger = CSLogger(CSLogLevel.OFF),
            healthEventLoggerListener = fakeCSHealthEventLoggerListener,
            healthEventFactory = fakeCSHealthEventFactory,
            appVersion = fakeCSMetaProvider.app.version,
            appVersionPreference = fakeCSAppVersionSharedPref
        )
    }

    @Test
    public fun `verify getAggregate events`() {
        coroutineRule.testDispatcher.runBlockingTest {
            val events = sut.getAggregateEvents()
            assertTrue(events.size == 1)

            events.forEachIndexed { index, event ->
                assertTrue(event.eventName == CSEventNamesConstant.Flushed.ClickStreamEventReceived.value)
                assertTrue(event.healthDetails.eventBatchGuidsList.containsAll(fakeCSHealthEventDTOs.map { it.eventBatchGuid }))
                assertTrue(event.healthDetails.eventGuidsList.containsAll(fakeCSHealthEventDTOs.map { it.eventGuid }))
                assertTrue(event.healthMeta.app.version == fakeCSHealthEventDTOs[index].appVersion)
                assertTrue(event.healthMeta.customer.currentCountry == fakeCSInfo.userInfo.currentCountry)
                assertTrue(event.healthMeta.customer.email == fakeCSInfo.userInfo.email)
                assertTrue(event.healthMeta.customer.identity == fakeCSInfo.userInfo.identity)
                assertTrue(event.healthMeta.customer.signedUpCountry == fakeCSInfo.userInfo.signedUpCountry)
                assertTrue(event.healthMeta.device.deviceMake == fakeCSInfo.deviceInfo.getDeviceManufacturer())
                assertTrue(event.healthMeta.device.deviceModel == fakeCSInfo.deviceInfo.getDeviceModel())
                assertTrue(event.healthMeta.device.operatingSystem == fakeCSInfo.deviceInfo.getOperatingSystem())
                assertTrue(event.healthMeta.device.operatingSystemVersion == fakeCSInfo.deviceInfo.getSDKVersion())
                uuidRegex.matcher(event.healthMeta.eventGuid).matches()
                assertTrue(event.healthMeta.location.latitude == fakeCSInfo.locationInfo.latitude)
                assertTrue(event.healthMeta.location.longitude == fakeCSInfo.locationInfo.longitude)
                assertTrue(event.healthMeta.session.sessionId == fakeCSInfo.sessionInfo.sessionID)
                assertTrue(event.numberOfBatches == fakeCSHealthEventDTOs.map { it.eventBatchGuid }.size.toLong())
                assertTrue(event.numberOfEvents == fakeCSHealthEventDTOs.map { it.eventGuid }.size.toLong())
                assertFalse(event.traceDetails.hasErrorDetails())
            }
        }
    }

    @Test
    public fun `verify getInstant events`() {
        coroutineRule.testDispatcher.runBlockingTest {
            val events = sut.getInstantEvents()
            assertTrue(events.isNotEmpty())

            events.forEachIndexed { index, event ->
                assertTrue(event.eventName == CSEventNamesConstant.Flushed.ClickStreamEventReceived.value)
                assertTrue(event.healthDetails.eventGuidsList.contains(fakeCSHealthEventDTOs[index].eventGuid))
                assertTrue(event.healthMeta.app.version == fakeCSHealthEventDTOs[index].appVersion)
                assertTrue(event.healthMeta.customer.currentCountry == fakeCSInfo.userInfo.currentCountry)
                assertTrue(event.healthMeta.customer.email == fakeCSInfo.userInfo.email)
                assertTrue(event.healthMeta.customer.identity == fakeCSInfo.userInfo.identity)
                assertTrue(event.healthMeta.customer.signedUpCountry == fakeCSInfo.userInfo.signedUpCountry)
                assertTrue(event.healthMeta.device.deviceMake == fakeCSInfo.deviceInfo.getDeviceManufacturer())
                assertTrue(event.healthMeta.device.deviceModel == fakeCSInfo.deviceInfo.getDeviceModel())
                assertTrue(event.healthMeta.device.operatingSystem == fakeCSInfo.deviceInfo.getOperatingSystem())
                assertTrue(event.healthMeta.device.operatingSystemVersion == fakeCSInfo.deviceInfo.getSDKVersion())
                uuidRegex.matcher(event.healthMeta.eventGuid).matches()
                assertTrue(event.healthMeta.location.latitude == fakeCSInfo.locationInfo.latitude)
                assertTrue(event.healthMeta.location.longitude == fakeCSInfo.locationInfo.longitude)
                assertTrue(event.healthMeta.session.sessionId == fakeCSInfo.sessionInfo.sessionID)
                assertTrue(event.numberOfBatches == 0L)
                assertTrue(event.numberOfEvents == 1L)
                assertTrue(event.traceDetails.errorDetails.reason == fakeCSHealthEventDTOs[index].error)
                assertTrue(event.traceDetails.timeToConnection == fakeCSHealthEventDTOs[index].timeToConnection.toString())
            }
        }
    }
}