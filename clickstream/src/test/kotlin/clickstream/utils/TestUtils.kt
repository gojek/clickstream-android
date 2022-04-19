package clickstream.utils

import com.tinder.scarlet.Event.OnLifecycle.StateChange
import com.tinder.scarlet.Event.OnStateChange
import com.tinder.scarlet.Event.OnWebSocket
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.Message
import com.tinder.scarlet.ShutdownReason
import com.tinder.scarlet.State
import com.tinder.scarlet.Stream
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.WebSocket.Event.OnConnectionClosed
import com.tinder.scarlet.WebSocket.Event.OnConnectionClosing
import com.tinder.scarlet.WebSocket.Event.OnMessageReceived
import kotlinx.coroutines.flow.Flow
import org.assertj.core.api.Assertions.assertThat

public fun <T : Any> Flow<T>.flowTest(): TestFlowObserver<T> = TestFlowObserver(this)

public fun <T : Any> Stream<T>.streamTest(): TestStreamObserver<T> = TestStreamObserver(this)

public inline fun <reified T : Any> any(noinline assertion: T.() -> Unit = {}): ValueAssert<T> =
    ValueAssert<T>()
        .assert { assertThat(this).isInstanceOf(T::class.java) }
        .assert(assertion)

public inline fun <reified T : Lifecycle.State> ValueAssert<StateChange<*>>.withLifecycleState(): ValueAssert<StateChange<*>> =
    assert {
        assertThat(state).isInstanceOf(T::class.java)
    }

public inline fun <reified T : WebSocket.Event> ValueAssert<OnWebSocket.Event<*>>.withWebSocketEvent(): ValueAssert<OnWebSocket.Event<*>> =
    assert {
        assertThat(event).isInstanceOf(T::class.java)
    }

public inline fun <reified T : State> ValueAssert<OnStateChange<*>>.withState(): ValueAssert<OnStateChange<*>> =
    assert {
        assertThat(state).isInstanceOf(T::class.java)
    }

public fun ValueAssert<OnMessageReceived>.containingText(
    expectedText: String
): ValueAssert<OnMessageReceived> =
    assert {
        assertThat(message).isInstanceOf(Message.Text::class.java)
        val (text) = message as Message.Text
        assertThat(text).isEqualTo(expectedText)
    }

public fun ValueAssert<OnMessageReceived>.containingBytes(
    expectedBytes: ByteArray
): ValueAssert<OnMessageReceived> =
    assert {
        assertThat(message).isInstanceOf(Message.Bytes::class.java)
        val (bytes) = message as Message.Bytes
        assertThat(bytes).isEqualTo(expectedBytes)
    }

public fun ValueAssert<OnConnectionClosing>.withClosingReason(
    expectedShutdownReason: ShutdownReason
): ValueAssert<OnConnectionClosing> = assert {
    assertThat(shutdownReason).isEqualTo(expectedShutdownReason)
}

public fun ValueAssert<OnConnectionClosed>.withClosedReason(
    expectedShutdownReason: ShutdownReason
): ValueAssert<OnConnectionClosed> = assert {
    assertThat(shutdownReason).isEqualTo(expectedShutdownReason)
}
