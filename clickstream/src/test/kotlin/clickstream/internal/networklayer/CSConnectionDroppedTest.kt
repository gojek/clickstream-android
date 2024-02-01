package clickstream.internal.networklayer

import clickstream.internal.eventscheduler.CSEventData
import clickstream.internal.networklayer.proto.raccoon.SendEventRequest
import clickstream.internal.networklayer.proto.raccoon.SendEventResponse
import clickstream.internal.utils.CSFlowStreamAdapterFactory
import clickstream.internal.utils.CSTimeStampMessageBuilder
import clickstream.model.CSEvent
import clickstream.proto.App
import clickstream.proto.User
import clickstream.utils.TestFlowObserver
import clickstream.utils.any
import clickstream.utils.containingBytes
import clickstream.utils.flowTest
import clickstream.utils.newWebSocketFactory
import com.google.protobuf.Timestamp
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.ShutdownReason
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.messageadapter.protobuf.ProtobufMessageAdapter
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
internal class CSConnectionDroppedTest {
    @get:Rule
    public val mockWebServer: MockWebServer = MockWebServer()
    private val serverUrlString by lazy { mockWebServer.url("/").toString() }

    private val serverLifecycleRegistry = LifecycleRegistry()
    private lateinit var server: CSEventService
    private lateinit var serverEventObserver: TestFlowObserver<WebSocket.Event>

    private val clientLifecycleRegistry = LifecycleRegistry()
    private lateinit var client: CSEventService
    private lateinit var clientEventObserver: TestFlowObserver<WebSocket.Event>

    @Test
    public fun send_givenConnectionIsEstablished_shouldBeReceivedByTheServer() {
        // Given
        givenConnectionIsEstablished()
        val testResponse: TestFlowObserver<SendEventResponse> = server.observeResponse().flowTest()

        val eventRequest1: SendEventRequest = generatedEvent("1")
        val eventRequest2: SendEventRequest = generatedEvent("2")

        // When
        val event1 = client.sendEvent(eventRequest1)
        val event2 = client.sendEvent(eventRequest2)
        val event3 = client.sendEvent(eventRequest1)
        val event4 = client.sendEvent(eventRequest2)

        // Then
        Assertions.assertThat(event1).isTrue
        Assertions.assertThat(event2).isTrue
        Assertions.assertThat(event3).isTrue
        Assertions.assertThat(event4).isTrue

        Assertions.assertThat(testResponse.values).allSatisfy { e ->
            e is SendEventResponse
        }

        serverLifecycleRegistry.onNext(Lifecycle.State.Stopped.WithReason(ShutdownReason.GRACEFUL))
        serverEventObserver.awaitValues(
            any<WebSocket.Event.OnConnectionOpened<*>>(),
            any<WebSocket.Event.OnMessageReceived>().containingBytes(eventRequest1.toByteArray()),
            any<WebSocket.Event.OnMessageReceived>().containingBytes(eventRequest2.toByteArray()),
            any<WebSocket.Event.OnMessageReceived>().containingBytes(eventRequest1.toByteArray()),
            any<WebSocket.Event.OnMessageReceived>().containingBytes(eventRequest2.toByteArray()),
            any<WebSocket.Event.OnConnectionClosing>(),
            any<WebSocket.Event.OnConnectionClosed>()
        )
    }

    private fun generatedEvent(guid: String): SendEventRequest {
        val event = CSEvent(
            guid = guid,
            timestamp = Timestamp.getDefaultInstance(),
            message = User.newBuilder()
                .setApp(
                    App.newBuilder()
                        .setVersion("4.35.0")
                ).build()
        )
        val (eventData, eventHealthData) = CSEventData.create(event)
        return transformToEventRequest(eventData = listOf(eventData))
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

    private fun blockUntilConnectionIsEstablish() {
        clientEventObserver.awaitValues(
            any<WebSocket.Event.OnConnectionOpened<*>>()
        )
        serverEventObserver.awaitValues(
            any<WebSocket.Event.OnConnectionOpened<*>>()
        )
    }

    private fun transformToEventRequest(eventData: List<CSEventData>): SendEventRequest {
        return SendEventRequest.newBuilder().apply {
            reqGuid = "1011"
            sentTime = CSTimeStampMessageBuilder.build(System.currentTimeMillis())
            addAllEvents(eventData.map { it.event() })
        }.build()
    }
}