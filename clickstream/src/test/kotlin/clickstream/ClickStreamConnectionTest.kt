package clickstream

import clickstream.internal.eventscheduler.CSEventData
import clickstream.internal.networklayer.CSEventService
import clickstream.internal.utils.CSFlowStreamAdapterFactory
import clickstream.internal.utils.CSTimeStampMessageBuilder
import clickstream.utils.TestFlowObserver
import clickstream.utils.any
import clickstream.utils.containingBytes
import clickstream.utils.flowTest
import clickstream.utils.newWebSocketFactory
import com.gojek.clickstream.common.App
import com.gojek.clickstream.common.EventMeta
import com.gojek.clickstream.de.EventRequest
import com.gojek.clickstream.de.common.EventResponse
import com.gojek.clickstream.products.events.AdCardEvent
import com.google.protobuf.Timestamp
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.WebSocket.Event
import com.tinder.scarlet.WebSocket.Event.OnConnectionOpened
import com.tinder.scarlet.WebSocket.Event.OnMessageReceived
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.messageadapter.protobuf.ProtobufMessageAdapter
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
public class ClickStreamConnectionTest {
    @get:Rule
    public val mockWebServer: MockWebServer = MockWebServer()
    private val serverUrlString by lazy { mockWebServer.url("/").toString() }

    private val serverLifecycleRegistry = LifecycleRegistry()
    private lateinit var server: CSEventService
    private lateinit var serverEventObserver: TestFlowObserver<Event>

    private val clientLifecycleRegistry = LifecycleRegistry()
    private lateinit var client: CSEventService
    private lateinit var clientEventObserver: TestFlowObserver<Event>

    @Test
    public fun send_givenConnectionIsEstablished_shouldBeReceivedByTheServer() {
        // Given
        givenConnectionIsEstablished()
        val testResponse: TestFlowObserver<EventResponse> = server.observeResponse().flowTest()

        val eventRequest1: EventRequest = generatedEvent("1")
        val eventRequest2: EventRequest = generatedEvent("2")

        // When
        val event1 = client.sendEvent(eventRequest1)
        val event2 = client.sendEvent(eventRequest2)
        val event3 = client.sendEvent(eventRequest1)
        val event4 = client.sendEvent(eventRequest2)

        // Then
        assertThat(event1).isTrue
        assertThat(event2).isTrue
        assertThat(event3).isTrue
        assertThat(event4).isTrue

        serverEventObserver.awaitValues(
            any<OnConnectionOpened<*>>(),
            any<OnMessageReceived>().containingBytes(eventRequest1.toByteArray()),
            any<OnMessageReceived>().containingBytes(eventRequest2.toByteArray()),
            any<OnMessageReceived>().containingBytes(eventRequest1.toByteArray()),
            any<OnMessageReceived>().containingBytes(eventRequest2.toByteArray())
        )

        assertThat(testResponse.values).allSatisfy { e ->
            e is EventResponse
        }
    }

    private fun generatedEvent(guid: String): EventRequest {
        val event = CSEvent(
            guid = guid,
            timestamp = Timestamp.getDefaultInstance(),
            message = AdCardEvent.newBuilder()
                .setMeta(
                    EventMeta.newBuilder()
                        .setApp(App.newBuilder().setVersion("4.35.0"))
                        .build()
                )
                .build()
        )
        val eventData = CSEventData.create(event)
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
            any<OnConnectionOpened<*>>()
        )
        serverEventObserver.awaitValues(
            any<OnConnectionOpened<*>>()
        )
    }

    private fun transformToEventRequest(eventData: List<CSEventData>): EventRequest {
        return EventRequest.newBuilder().apply {
            reqGuid = "1011"
            sentTime = CSTimeStampMessageBuilder.build(System.currentTimeMillis())
            addAllEvents(eventData.map { it.event() })
        }.build()
    }
}