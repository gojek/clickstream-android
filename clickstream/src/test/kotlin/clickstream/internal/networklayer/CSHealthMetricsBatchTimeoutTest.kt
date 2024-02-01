package clickstream.internal.networklayer

import clickstream.config.CSNetworkConfig
import clickstream.fake.fakeCSInfo
import clickstream.health.constant.CSEventNamesConstant
import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.model.CSHealthEventDTO
import clickstream.health.time.CSTimeStampGenerator
import clickstream.internal.networklayer.proto.raccoon.Event
import clickstream.internal.networklayer.proto.raccoon.SendEventRequest
import clickstream.internal.utils.CSFlowStreamAdapterFactory
import clickstream.logger.CSLogLevel
import clickstream.logger.CSLogger
import clickstream.proto.User
import clickstream.utils.CoroutineTestRule
import clickstream.utils.TestFlowObserver
import clickstream.utils.flowTest
import clickstream.utils.newWebSocketFactory
import com.google.protobuf.Timestamp
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.messageadapter.protobuf.ProtobufMessageAdapter
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
internal class CSHealthMetricsBatchTimeoutTest {

    @get:Rule
    public val mockWebServer: MockWebServer = MockWebServer()
    @get:Rule
    public val coroutineRule: CoroutineTestRule = CoroutineTestRule()

    private val info = fakeCSInfo()

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
    public fun verifyNoEventsAreNotCrashing() = runBlocking {
        // Given
        givenConnectionIsEstablished()

        // When
        val eventRequest = SendEventRequest.newBuilder()
            .setReqGuid("1234")
            .setSentTime(Timestamp.getDefaultInstance())
            .addEvents(
                Event.newBuilder()
                    .setEventBytes(User.newBuilder().build().toByteString())
                    .setType("AdcardEvent")
                    .build()
            )
            .build()

        // Then
        object : CSRetryableCallback(
            networkConfig = CSNetworkConfig.default(OkHttpClient()),
            eventService = client,
            eventRequest = eventRequest,
            dispatcher = coroutineRule.testDispatcher,
            timeStampGenerator = timestamp,
            logger = CSLogger(CSLogLevel.OFF),
            healthEventRepository = health,
            info = info,
            coroutineScope = coroutineRule.scope,
            eventGuids = "1234"
        ) {
            override fun onSuccess(guid: String) { /*No Op*/ }

            override fun onFailure(throwable: Throwable, guid: String) { /*No Op*/ }
        }

        coroutineRule.scope.advanceTimeBy(10_000)

        val batchTimeout = CSHealthEventDTO(
            eventName = CSEventNamesConstant.Instant.ClickStreamEventBatchTimeout.value,
            eventType = CSEventTypesConstant.INSTANT,
            appVersion = info.appInfo.appVersion,
            eventBatchGuid = "1234",
            error = "SocketTimeout"
        )

        val batchSent = CSHealthEventDTO(
            eventName = CSEventNamesConstant.AggregatedAndFlushed.ClickStreamBatchSent.value,
            eventType = CSEventTypesConstant.AGGREGATE,
            appVersion = info.appInfo.appVersion,
            eventBatchGuid = "1234",
            eventGuid = "1234"
        )

        verify(health).insertHealthEvent(batchTimeout)
        verify(health, times(2)).insertHealthEvent(batchSent)
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
            clickstream.utils.any<WebSocket.Event.OnConnectionOpened<*>>()
        )
        serverEventObserver.awaitValues(
            clickstream.utils.any<WebSocket.Event.OnConnectionOpened<*>>()
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