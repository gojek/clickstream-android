package clickstream.internal.analytics

import clickstream.api.CSInfo
import clickstream.fake.FakeHealthRepository
import clickstream.fake.fakeAppInfo
import clickstream.fake.fakeCSHealthEventConfig
import clickstream.fake.fakeCSInfo
import clickstream.fake.fakeCSSessionInfo
import clickstream.fake.fakeUserInfo
import clickstream.health.constant.CSErrorConstant
import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.constant.CSHealthEventName
import clickstream.health.intermediate.CSHealthEventLoggerListener
import clickstream.health.intermediate.CSHealthEventProcessor
import clickstream.health.intermediate.CSMemoryStatusProvider
import clickstream.health.internal.CSGuIdGenerator
import clickstream.health.internal.factory.CSHealthEventFactory
import clickstream.health.internal.factory.DefaultCSHealthEventFactory
import clickstream.health.internal.processor.CSHealthEventProcessorImpl
import clickstream.health.model.CSEventForHealth
import clickstream.health.model.CSHealthEvent
import clickstream.health.model.CSHealthEventConfig
import clickstream.health.model.EXTERNAL
import clickstream.health.model.INTERNAL
import clickstream.health.time.CSHealthTimeStampGenerator
import clickstream.logger.CSLogLevel.OFF
import clickstream.logger.CSLogger
import clickstream.util.CSAppVersionSharedPref
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

@ExperimentalCoroutinesApi
internal class CSHealthEventProcessorTest {

    private val csHealthEventRepository = FakeHealthRepository()
    private val csHealthEventFactory = fakeCSHealthEventFactory()
    private val csAppVersionSharedPref = mock(CSAppVersionSharedPref::class.java)
    private val memoryStatusProvider = mock(CSMemoryStatusProvider::class.java)
    private val healthEventLogger = mock(CSHealthEventLoggerListener::class.java)
    private val loggerMock = mock(CSLogger::class.java)

    private lateinit var sut: CSHealthEventProcessor

    @Before
    fun setup() {
        runBlocking {
            whenever(memoryStatusProvider.isLowMemory()).thenReturn(false)
        }
    }

    @Test
    fun `Given app version is less than minimum app version verify that health is not tracked`() {
        runBlocking {
            sut = getEventProcessor(
                fakeCSInfo().copy(
                    appInfo = fakeAppInfo.copy(appVersion = "4.38.0"),
                    sessionInfo = fakeCSSessionInfo.copy(sessionID = "566")
                ),
                fakeCSHealthEventConfig.copy(
                    minTrackedVersion = "5.1",
                    randomUserIdRemainder = listOf(6, 9)
                )
            )

            val events = fakeCSHealthEvent(1)

            sut.insertBatchEvent(events, 1)
            sut.insertBatchEvent(events, emptyList())
            sut.insertNonBatchEvent(events)

            assert(csHealthEventRepository.getEventCount(CSEventTypesConstant.AGGREGATE) == 0)
        }
    }

    @Test
    fun `Given user is not whitelisted verify that health is not tracked`() {
        runBlocking {
            sut = getEventProcessor(
                fakeCSInfo().copy(
                    appInfo = fakeAppInfo.copy(appVersion = "4.38.0"),
                    userInfo = fakeUserInfo().copy(identity = 122345)
                ),
                fakeCSHealthEventConfig.copy(
                    minTrackedVersion = "4.60.0",
                    randomUserIdRemainder = listOf(1, 0)
                )
            )

            val events = fakeCSHealthEvent(1)

            sut.insertBatchEvent(events, 1)
            sut.insertBatchEvent(events, emptyList())
            sut.insertNonBatchEvent(events)

            assert(csHealthEventRepository.getEventCount(CSEventTypesConstant.AGGREGATE) == 0)
        }
    }

    @Test
    fun `Given app is low on memory verify that health is not tracked`() {
        runBlocking {
            sut = getEventProcessor(
                fakeCSInfo().copy(
                    appInfo = fakeAppInfo.copy(appVersion = "4.60.0"),
                    userInfo = fakeUserInfo().copy(identity = 122345)
                ),
                fakeCSHealthEventConfig.copy(
                    minTrackedVersion = "4.38.0",
                    randomUserIdRemainder = listOf(5, 0)
                )
            )

            whenever(memoryStatusProvider.isLowMemory()).thenReturn(true)

            val events = fakeCSHealthEvent(1)

            sut.insertBatchEvent(events, 1)
            sut.insertBatchEvent(events, emptyList())
            sut.insertNonBatchEvent(events)

            assert(csHealthEventRepository.getEventCount(CSEventTypesConstant.AGGREGATE) == 0)
        }
    }

    @Test
    fun `Given user is whitelisted and app version is correct for health verify that health is tracked`() {
        runBlocking {
            sut = getEventProcessor(
                fakeCSInfo().copy(
                    appInfo = fakeAppInfo.copy(appVersion = "4.60.0"),
                    userInfo = fakeUserInfo().copy(identity = 122345)
                ),
                fakeCSHealthEventConfig.copy(
                    minTrackedVersion = "4.38.0",
                    randomUserIdRemainder = listOf(5, 0)
                )
            )

            val events = fakeCSHealthEvent(1)

            sut.insertBatchEvent(events, 1)
            sut.insertBatchEvent(events, emptyList())
            sut.insertNonBatchEvent(events)

            assert(csHealthEventRepository.getEventCount(CSEventTypesConstant.AGGREGATE) == 3)
        }
    }

    @Test
    fun `Given destination is empty verify that health flow returns empty list`() {
        runBlocking {
            sut = getEventProcessor(
                fakeCSInfo().copy(
                    appInfo = fakeAppInfo.copy(appVersion = "4.60.0"),
                    userInfo = fakeUserInfo().copy(identity = 122345)
                ),
                fakeCSHealthEventConfig.copy(
                    minTrackedVersion = "4.38.0",
                    randomUserIdRemainder = listOf(5, 0),
                    destination = emptyList(),
                    verbosityLevel = "minimum"
                )
            )

            val batchId = UUID.randomUUID().toString()
            val csEvents = (0..50).map { fakeCSEvent(it.toString(), batchId) }

            val healthEvent = CSHealthEvent(
                eventName = CSHealthEventName.ClickStreamBatchSent.name,
                appVersion = "4.60.0",
                eventType = CSEventTypesConstant.AGGREGATE
            )

            sut.insertBatchEvent(healthEvent, csEvents)

            val list = sut.getHealthEventFlow(CSEventTypesConstant.AGGREGATE).toList()

            assert(list.isEmpty())
            verify(healthEventLogger, never()).logEvent(any(), any())
        }
    }

    @Test
    fun `Given destination is contains CS verify that health flow returns correct list`() {
        runBlocking {
            sut = getEventProcessor(
                fakeCSInfo().copy(
                    appInfo = fakeAppInfo.copy(appVersion = "4.60.0"),
                    userInfo = fakeUserInfo().copy(identity = 122345)
                ),
                fakeCSHealthEventConfig.copy(
                    minTrackedVersion = "4.38.0",
                    randomUserIdRemainder = listOf(5, 0),
                    destination = listOf(INTERNAL),
                    verbosityLevel = "minimum"
                )
            )

            val batchId = UUID.randomUUID().toString()
            val csEvents = (0..50).map { fakeCSEvent(it.toString(), batchId) }

            val healthEvent = CSHealthEvent(
                eventName = CSHealthEventName.ClickStreamBatchSent.value,
                appVersion = "4.60.0",
                eventType = CSEventTypesConstant.AGGREGATE,
            )

            sut.insertBatchEvent(healthEvent, csEvents)

            val list = sut.getHealthEventFlow(CSEventTypesConstant.AGGREGATE).toList()
            val fetchedHealth = list[0][0]

            assert(fetchedHealth.numberOfEvents == 51L)
            assert(fetchedHealth.numberOfBatches == 1L)
            assert(csHealthEventRepository.getEventCount(CSEventTypesConstant.AGGREGATE) == 0)
            verify(healthEventLogger, never()).logEvent(any(), any())
        }
    }

    @Test
    fun `Given destination is contains CT verify that health flow returns correct list`() {
        runBlocking {
            sut = getEventProcessor(
                fakeCSInfo().copy(
                    appInfo = fakeAppInfo.copy(appVersion = "4.60.0"),
                    userInfo = fakeUserInfo().copy(identity = 122345)
                ),
                fakeCSHealthEventConfig.copy(
                    minTrackedVersion = "4.38.0",
                    randomUserIdRemainder = listOf(5, 0),
                    destination = listOf(EXTERNAL),
                    verbosityLevel = "minimum"
                )
            )

            val batchId = UUID.randomUUID().toString()
            val csEvents = (0..50).map { fakeCSEvent(it.toString(), batchId) }

            val healthEvent = CSHealthEvent(
                eventName = CSHealthEventName.ClickStreamEventBatchTriggerFailed.value,
                appVersion = "4.60.0",
                eventType = CSEventTypesConstant.AGGREGATE,
            )

            sut.insertBatchEvent(healthEvent, csEvents)

            sut.getHealthEventFlow(CSEventTypesConstant.AGGREGATE).toList()

            verify(healthEventLogger, atLeast(1)).logEvent(any(), any())
        }
    }

    @Test
    fun `Given destination is contains CT verify that upstream listener is invoked with correct data`() {
        runBlocking {
            sut = getEventProcessor(
                fakeCSInfo().copy(
                    appInfo = fakeAppInfo.copy(appVersion = "4.60.0"),
                    userInfo = fakeUserInfo().copy(identity = 122345)
                ),
                fakeCSHealthEventConfig.copy(
                    minTrackedVersion = "4.38.0",
                    randomUserIdRemainder = listOf(5, 0),
                    destination = listOf(EXTERNAL),
                    verbosityLevel = "minimum"
                )
            )

            (1..7).forEach { _ ->
                val health = CSHealthEvent(
                    eventName = CSHealthEventName.ClickStreamEventBatchTriggerFailed.value,
                    appVersion = "4.60.0",
                    eventType = CSEventTypesConstant.AGGREGATE,
                    error = CSErrorConstant.LOW_BATTERY
                )
                sut.insertBatchEvent(health, 20)
            }

            (1..30).forEach { _ ->
                val health = CSHealthEvent(
                    eventName = CSHealthEventName.ClickStreamEventBatchTriggerFailed.value,
                    appVersion = "4.60.0",
                    eventType = CSEventTypesConstant.AGGREGATE,
                    error = CSErrorConstant.NETWORK_UNAVAILABLE
                )
                sut.insertBatchEvent(health, 20)
            }

            (1..28).forEach { _ ->
                val health = CSHealthEvent(
                    eventName = CSHealthEventName.ClickStreamEventBatchTriggerFailed.value,
                    appVersion = "4.60.0",
                    eventType = CSEventTypesConstant.AGGREGATE,
                    error = CSErrorConstant.SOCKET_NOT_OPEN
                )
                sut.insertBatchEvent(health, 30)
            }

            (1..19).forEach { _ ->
                val health = CSHealthEvent(
                    eventName = CSHealthEventName.ClickStreamConnectionFailed.value,
                    appVersion = "4.60.0",
                    eventType = CSEventTypesConstant.AGGREGATE,
                    error = "java io exception"
                )
                sut.insertBatchEvent(health, 34)
            }

            val list = sut.getHealthEventFlow(CSEventTypesConstant.AGGREGATE).toList()
            assert(list.isEmpty())

            verify(healthEventLogger, times(9)).logEvent(any(), any())
        }
    }

    @Test
    fun `Given health repository has no events verify that health flow returns empty list`() {
        runBlocking {
            sut = getEventProcessor(
                fakeCSInfo().copy(
                    appInfo = fakeAppInfo.copy(appVersion = "4.60.0"),
                    userInfo = fakeUserInfo().copy(identity = 122345)
                ),
                fakeCSHealthEventConfig.copy(
                    minTrackedVersion = "4.38.0",
                    randomUserIdRemainder = listOf(5, 0),
                    destination = listOf(EXTERNAL, INTERNAL),
                    verbosityLevel = "minimum"
                )
            )

            val list = sut.getHealthEventFlow(CSEventTypesConstant.AGGREGATE).toList()

            assert(list.isEmpty())
        }
    }

    @Test
    fun `Given verbosity is minimum verify that healthDetails is null `() {
        runBlocking {
            sut = getEventProcessor(
                fakeCSInfo().copy(
                    appInfo = fakeAppInfo.copy(appVersion = "4.60.0"),
                    userInfo = fakeUserInfo().copy(identity = 122345)
                ),
                fakeCSHealthEventConfig.copy(
                    minTrackedVersion = "4.38.0",
                    randomUserIdRemainder = listOf(5, 0),
                    destination = listOf(INTERNAL),
                    verbosityLevel = "minimum"
                )
            )

            val batchId = UUID.randomUUID().toString()
            val csEvents = (0..50).map { fakeCSEvent(it.toString(), batchId) }

            val healthEvent = CSHealthEvent(
                eventName = CSHealthEventName.ClickStreamBatchSent.value,
                appVersion = "4.60.0",
                eventType = CSEventTypesConstant.AGGREGATE
            )

            sut.insertBatchEvent(healthEvent, csEvents)

            val list = sut.getHealthEventFlow(CSEventTypesConstant.AGGREGATE).toList()
            val fetchedHealth = list[0][0]

            assert(!fetchedHealth.hasHealthDetails())
        }
    }

    @Test
    fun `Given verbosity is maximum verify that healthDetails is set correctly `() {
        runBlocking {
            sut = getEventProcessor(
                fakeCSInfo().copy(
                    appInfo = fakeAppInfo.copy(appVersion = "4.60.0"),
                    userInfo = fakeUserInfo().copy(identity = 122345)
                ),
                fakeCSHealthEventConfig.copy(
                    minTrackedVersion = "4.38.0",
                    randomUserIdRemainder = listOf(5, 0),
                    destination = listOf(INTERNAL),
                    verbosityLevel = "maximum"
                )
            )

            val batchId = UUID.randomUUID().toString()
            val csEvents = (0..50).map { fakeCSEvent(it.toString(), batchId) }

            val healthEvent = CSHealthEvent(
                eventName = CSHealthEventName.ClickStreamBatchSent.value,
                appVersion = "4.60.0",
                eventType = CSEventTypesConstant.AGGREGATE,
                eventBatchGuid = batchId
            )

            sut.insertBatchEvent(healthEvent, csEvents)

            val list = sut.getHealthEventFlow(CSEventTypesConstant.AGGREGATE).toList()
            val fetchedHealth = list[0][0]

            assert(fetchedHealth.hasHealthDetails())
            assert(fetchedHealth.healthDetails.eventGuidsList.isNotEmpty())
            assert(fetchedHealth.healthDetails.eventBatchGuidsList.isNotEmpty())
        }
    }

    @Test
    fun `Given app version is changes verify health events are deleted sucessfully`() {
        runBlocking {
            sut = getEventProcessor(
                fakeCSInfo().copy(
                    appInfo = fakeAppInfo.copy(appVersion = "4.60.0"),
                    userInfo = fakeUserInfo().copy(identity = 122345)
                ),
                fakeCSHealthEventConfig.copy(
                    minTrackedVersion = "4.38.0",
                    randomUserIdRemainder = listOf(5, 0),
                    verbosityLevel = "maximum"
                )
            )

            val healthEvent = (0..50).map { fakeCSHealthEvent(it) }

            csHealthEventRepository.insertHealthEventList(healthEvent)

            whenever(csAppVersionSharedPref.isAppVersionEqual(any())).thenReturn(false)
            CSHealthEventProcessorImpl.clearHealthEventsForVersionChange(
                csAppVersionSharedPref,
                "4.60.1",
                csHealthEventRepository,
                loggerMock
            )

            assert(csHealthEventRepository.getEventCount(CSEventTypesConstant.AGGREGATE) == 0)
        }
    }

    @Test
    fun `Given app version is not changed verify health events are not deleted`() {
        runBlocking {
            sut = getEventProcessor(
                fakeCSInfo().copy(
                    appInfo = fakeAppInfo.copy(appVersion = "4.60.0"),
                    userInfo = fakeUserInfo().copy(identity = 122345)
                ),
                fakeCSHealthEventConfig.copy(
                    minTrackedVersion = "4.38.0",
                    randomUserIdRemainder = listOf(5, 0),
                    verbosityLevel = "maximum"
                )
            )

            val healthEvent = (0..50).map { fakeCSHealthEvent(it) }

            csHealthEventRepository.insertHealthEventList(healthEvent)
            whenever(csAppVersionSharedPref.isAppVersionEqual(any())).thenReturn(true)
            CSHealthEventProcessorImpl.clearHealthEventsForVersionChange(
                csAppVersionSharedPref,
                "4.60.0",
                csHealthEventRepository,
                loggerMock
            )

            assert(csHealthEventRepository.getEventCount(CSEventTypesConstant.AGGREGATE) == 51)
        }
    }

    @Test
    fun `Given health events verify health meta is correct`() {
        runBlocking {
            sut = getEventProcessor(
                fakeCSInfo().copy(
                    appInfo = fakeAppInfo.copy(appVersion = "4.60.0"),
                    userInfo = fakeUserInfo().copy(identity = 122345)
                ),
                fakeCSHealthEventConfig.copy(
                    minTrackedVersion = "4.38.0",
                    randomUserIdRemainder = listOf(5, 0),
                    destination = listOf(INTERNAL),
                    verbosityLevel = "maximum"
                )
            )

            val healthEvent = fakeCSHealthEvent(1)
            sut.insertBatchEvent(healthEvent, 20)
            val healthList = sut.getHealthEventFlow(CSEventTypesConstant.AGGREGATE).toList()
            val healthProtoEvent = healthList[0][0]

            with(healthProtoEvent.healthMeta) {
                assert(customer.email == fakeCSInfo().userInfo.email)
                assert(app.version == fakeCSInfo().appInfo.appVersion)
                assert(session.sessionId == fakeCSInfo().sessionInfo.sessionID)
            }
        }
    }

    @Test
    fun `Given health config verify isAppVersionGreater function`() {
        assert(fakeCSHealthEventConfig.isAppVersionGreater("4.56", "5.0.1").not())
        assert(fakeCSHealthEventConfig.isAppVersionGreater("", "").not())
        assert(
            fakeCSHealthEventConfig.isAppVersionGreater(
                "4.56.1-Alpha-3456e4",
                "4.56.0-Alpha-3456e4"
            )
        )
        assert(fakeCSHealthEventConfig.isAppVersionGreater("4.36.1", "1"))
        assert(fakeCSHealthEventConfig.isAppVersionGreater("0.0.1", "0.0.4").not())
    }

    @Test
    fun `Given health config verify isHealthEnabledUser function`() {
        val config = fakeCSHealthEventConfig.copy(randomUserIdRemainder = listOf(1, 6, 9))
        assert(config.isHealthEnabledUser(22546))
        assert(config.isHealthEnabledUser(4559))
        assert(config.isHealthEnabledUser(21091))
        assert(config.isHealthEnabledUser(0).not())
        assert(config.isHealthEnabledUser(288764).not())
        assert(config.isHealthEnabledUser(288763).not())
    }

    private fun getEventProcessor(
        csInfo: CSInfo,
        healthConfig: CSHealthEventConfig = fakeCSHealthEventConfig.copy(
            destination = listOf(EXTERNAL)
        ),
    ) = CSHealthEventProcessorImpl(
        healthEventRepository = csHealthEventRepository,
        healthEventConfig = healthConfig,
        info = csInfo,
        logger = CSLogger(OFF),
        healthEventFactory = csHealthEventFactory,
        csHealthEventLogger = healthEventLogger,
        memoryStatusProvider = memoryStatusProvider,
    )

    private fun fakeCSHealthEvent(
        id: Int,
        eventName: CSHealthEventName = CSHealthEventName.ClickStreamBatchSent
    ): CSHealthEvent {
        return CSHealthEvent(
            healthEventID = id,
            eventName = eventName.value,
            eventType = CSEventTypesConstant.AGGREGATE,
            timestamp = System.currentTimeMillis().toString(),
            error = "",
            sessionId = "13455",
            count = 0,
            networkType = "LTE",
            batchSize = 1,
            appVersion = "4.37.0"
        )
    }

    private fun fakeCSEvent(
        id: String = UUID.randomUUID().toString(),
        reqId: String = UUID.randomUUID().toString()
    ): CSEventForHealth {
        return CSEventForHealth(
            eventGuid = id,
            batchGuid = reqId,
        )
    }

    private fun fakeCSHealthEventFactory(): CSHealthEventFactory {
        return DefaultCSHealthEventFactory(object : CSGuIdGenerator {
            override fun getId(): String {
                return UUID.randomUUID().toString()
            }
        }, object : CSHealthTimeStampGenerator {

            override fun getTimeStamp(): Long {
                return System.currentTimeMillis()
            }
        }, fakeCSInfo())
    }
}