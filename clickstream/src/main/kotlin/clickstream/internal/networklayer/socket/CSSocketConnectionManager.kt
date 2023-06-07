package clickstream.internal.networklayer.socket

import android.app.Application
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.ShutdownReason
import com.tinder.scarlet.lifecycle.LifecycleRegistry

private const val SHUTDOWN_REASON_CODE = 1001

/**
 * Socket connection manager for clickstream.
 * connect -> establish socket connection.
 * disconnect -> disconnect socket connection.
 */
internal class CSSocketConnectionManager(
    private val lifecycleRegistry: LifecycleRegistry,
    application: Application,
) : Lifecycle by lifecycleRegistry.combineWith(CSConnectivityOnLifecycle(application)) {

    /**
     * Connecting socket.
     */
    fun connect() {
        lifecycleRegistry.onNext(Lifecycle.State.Started)
    }

    /**
     * Disconnecting socket.
     */
    fun disconnect() {
        lifecycleRegistry.onNext(
            Lifecycle.State.Stopped.WithReason(
                ShutdownReason(SHUTDOWN_REASON_CODE, "Gracefully disconnected")
            )
        )
    }
}