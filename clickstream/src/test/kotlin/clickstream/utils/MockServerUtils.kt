@file:Suppress("MatchingDeclarationName")

package clickstream.utils

import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.websocket.okhttp.OkHttpWebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

public class MockWebServerOkHttpWebSocketConnectionEstablisher(
    private val mockWebServer: MockWebServer
) : OkHttpWebSocket.ConnectionEstablisher {

    override fun establishConnection(webSocketListener: WebSocketListener) {
        mockWebServer.enqueue(MockResponse().withWebSocketUpgrade(webSocketListener))
    }
}

public fun MockWebServer.newWebSocketFactory(): WebSocket.Factory =
    OkHttpWebSocket.Factory(MockWebServerOkHttpWebSocketConnectionEstablisher(this))
