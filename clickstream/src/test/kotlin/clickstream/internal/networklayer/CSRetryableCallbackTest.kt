package clickstream.internal.networklayer

import clickstream.config.CSNetworkConfig
import clickstream.fake.fakeCSInfo
import clickstream.health.time.CSTimeStampGenerator
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.internal.utils.CSFlowStreamAdapterFactory
import clickstream.logger.CSLogLevel.OFF
import clickstream.logger.CSLogger
import clickstream.utils.CoroutineTestRule
import clickstream.utils.TestFlowObserver
import clickstream.utils.any
import clickstream.utils.flowTest
import clickstream.utils.newWebSocketFactory
import com.gojek.clickstream.de.EventRequest
import com.nhaarman.mockitokotlin2.mock
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.WebSocket.Event.OnConnectionOpened
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.messageadapter.protobuf.ProtobufMessageAdapter
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
public class CSRetryableCallbackTest {

    @get:Rule
    public val mockWebServer: MockWebServer = MockWebServer()
    @get:Rule
    public val coroutineRule: CoroutineTestRule = CoroutineTestRule()

    private val serverUrlString by lazy { mockWebServer.url("/").toString() }
    private val timestamp = mock<CSTimeStampGenerator>()
    private val health = mock<CSHealthEventRepository>()

    private val serverLifecycleRegistry = LifecycleRegistry()
    private lateinit var server: CSEventService
    private lateinit var serverEventObserver: TestFlowObserver<WebSocket.Event>

    private val clientLifecycleRegistry = LifecycleRegistry()
    private lateinit var client: CSEventService
    private lateinit var clientEventObserver: TestFlowObserver<WebSocket.Event>

    @Test
    public fun verifyNoEventsAreNotCrashing() {
        // Given
        givenConnectionIsEstablished()

        // When
        val eventRequest = EventRequest.newBuilder()
            .build()

        // Then
        object : CSRetryableCallback(
            networkConfig = CSNetworkConfig.default(OkHttpClient()),
            eventService = client,
            eventRequest = eventRequest,
            dispatcher = coroutineRule.testDispatcher,
            timeStampGenerator = timestamp,
            logger = CSLogger(OFF),
            healthEventRepository = health,
            info = fakeCSInfo(),
            coroutineScope = coroutineRule.scope
        ) {
            override fun onSuccess(guid: String) { /*No Op*/
            }

            override fun onFailure(throwable: Throwable, guid: String) { /*No Op*/
            }
        }
    }

    private fun givenConnectionIsEstablished() {
        createClientAndServer()
        serverLifecycleRegistry.onNext(Lifecycle.State.Started)
        clientLifecycleRegistry.onNext(Lifecycle.State.Started)
        blockUntilConnectionIsEstablish()
    }

    private fun createClientAndServer() {
        server = createServer()
        serverEventObserver = server.observeSocketState().flowTest()
        client = createClient()
        clientEventObserver = client.observeSocketState().flowTest()
    }

    private fun blockUntilConnectionIsEstablish() {
        clientEventObserver.awaitValues(
            any<OnConnectionOpened<*>>()
        )
        serverEventObserver.awaitValues(
            any<OnConnectionOpened<*>>()
        )
    }

    private fun createServer(): CSEventService {
        val webSocketFactory = mockWebServer.newWebSocketFactory()
        val scarlet = Scarlet.Builder()
            .webSocketFactory(webSocketFactory)
            .lifecycle(serverLifecycleRegistry)
            .addStreamAdapterFactory(CSFlowStreamAdapterFactory())
            .addMessageAdapterFactory(ProtobufMessageAdapter.Factory())
            .build()
        return scarlet.create()
    }

    private fun createClient(): CSEventService {
        val okHttpClient = OkHttpClient.Builder()
            .writeTimeout(500, TimeUnit.MILLISECONDS)
            .readTimeout(500, TimeUnit.MILLISECONDS)
            .build()
        val webSocketFactory = okHttpClient.newWebSocketFactory(serverUrlString)
        val scarlet = Scarlet.Builder()
            .webSocketFactory(webSocketFactory)
            .lifecycle(clientLifecycleRegistry)
            .addStreamAdapterFactory(CSFlowStreamAdapterFactory())
            .addMessageAdapterFactory(ProtobufMessageAdapter.Factory())
            .build()
        return scarlet.create()
    }
}
