package clickstream.internal.networklayer

import clickstream.connection.CSSocketConnectionListener
import clickstream.fake.fakeCSInfo
import clickstream.health.constant.CSEventNamesConstant
import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.model.CSHealthEventDTO
import clickstream.utils.CoroutineTestRule
import com.gojek.clickstream.de.EventRequest
import com.gojek.clickstream.de.common.EventResponse
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
internal class CSHealthMetricsConnectionOpenedTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()
    @get:Rule
    val mockWebServer: MockWebServer = MockWebServer()

    private val connectionListener = mock<CSSocketConnectionListener>()
    private val healthEventRepository = mock<CSHealthEventRepository>()
    private val server = FakeCSEventService()
    private val info = fakeCSInfo()

    private val networkRepository: CSNetworkRepository by lazy {
        CSNetworkRepositoryImpl(
            networkConfig = mock(),
            eventService = server,
            dispatcher = coroutineRule.testDispatcher,
            timeStampGenerator = mock(),
            logger = mock(),
            healthEventRepository = healthEventRepository,
            info = info
        )
    }

    private val networkManager: CSNetworkManager by lazy {
        CSNetworkManager(
            appLifeCycle = mock(),
            networkRepository = networkRepository,
            dispatcher = coroutineRule.testDispatcher,
            logger = mock(),
            healthEventRepository = healthEventRepository,
            info = info,
            connectionListener = connectionListener
        )
    }

    @Test
    fun `verify connection opened dto`() = runBlocking {
        networkManager.observeSocketConnectionState()

        val connAttemptDto = CSHealthEventDTO(
            eventName = CSEventNamesConstant.Instant.ClickStreamConnectionAttempt.value,
            eventType = CSEventTypesConstant.INSTANT,
            appVersion = info.appInfo.appVersion
        )
        val connSuccessDto = CSHealthEventDTO(
            eventName = CSEventNamesConstant.Instant.ClickStreamConnectionSuccess.value,
            eventType = CSEventTypesConstant.INSTANT,
            appVersion = info.appInfo.appVersion,
            timeToConnection = networkManager.endConnectedTime - networkManager.startConnectingTime
        )

        verify(healthEventRepository).insertHealthEvent(connAttemptDto)
        verify(healthEventRepository).insertHealthEvent(connSuccessDto)
        verify(connectionListener, times(2)).onEventChanged(any())
    }

    private class FakeCSEventService : CSEventService {
        override fun observeResponse(): Flow<EventResponse> {
            throw IllegalAccessException("broken")
        }

        override fun observeSocketState(): Flow<WebSocket.Event> {
            return flow {
                emit(WebSocket.Event.OnConnectionOpened(Unit))
            }
        }

        override fun sendEvent(streamBatchEvents: EventRequest): Boolean {
            throw IllegalAccessException("broken")
        }
    }
}